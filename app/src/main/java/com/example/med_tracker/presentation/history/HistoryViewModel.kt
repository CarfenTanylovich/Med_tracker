package com.example.med_tracker.presentation.history

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.data.repository.MedicationRepository
import com.example.med_tracker.domain.model.TodayIntakeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HistoryUiState(
    val logs: List<TodayIntakeItem> = emptyList(),
    val adherencePercentage: Int = 0,
    val totalCount: Int = 0,
    val takenCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MedicationRepository(AppDatabase.getInstance(application))
    private val periodDays = MutableStateFlow(7) // По умолчанию 7 дней

    val uiState: StateFlow<HistoryUiState> = periodDays.flatMapLatest { days ->
        val endCal = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }
        repository.getHistoryLogs(startCal.timeInMillis, endCal.timeInMillis)
    }.combine(periodDays) { logs, _ ->
        val total = logs.count { it.status != IntakeStatus.PENDING }
        val taken = logs.count { it.status == IntakeStatus.TAKEN }
        val percentage = if (total > 0) (taken * 100) / total else 0
        HistoryUiState(
            logs = logs.sortedByDescending { it.scheduledTimeMillis },
            adherencePercentage = percentage,
            totalCount = total,
            takenCount = taken
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setPeriod(days: Int) {
        periodDays.value = days
    }
}
