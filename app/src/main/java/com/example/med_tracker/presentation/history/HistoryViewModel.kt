package com.example.med_tracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.data.repository.MedicationRepository
import com.example.med_tracker.domain.model.TodayIntakeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryUiState(
    val logs: List<TodayIntakeItem> = emptyList(),
    val adherencePercentage: Int = 0,
    val totalCount: Int = 0,
    val takenCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    private val _periodDays = MutableStateFlow(7)

    val uiState: StateFlow<HistoryUiState> = _periodDays
        .flatMapLatest { days ->
            repository.getHistoryLogsFlow(days)
        }
        .map { rawLogs ->
            val currentTime = System.currentTimeMillis()

            // Если время вышло, а статус всё еще PENDING — для истории это MISSED
            val processedLogs = rawLogs.map { item ->
                if (item.status == IntakeStatus.PENDING && item.scheduledTimeMillis <= currentTime) {
                    item.copy(status = IntakeStatus.MISSED)
                } else {
                    item
                }
            }.filter { it.scheduledTimeMillis <= currentTime } // Будущие приемы в историю не идут

            val taken = processedLogs.count { it.status == IntakeStatus.TAKEN }
            val total = processedLogs.size
            val percentage = if (total > 0) (taken * 100) / total else 0

            HistoryUiState(
                logs = processedLogs.sortedByDescending { it.scheduledTimeMillis },
                adherencePercentage = percentage,
                totalCount = total,
                takenCount = taken
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )

    fun setPeriod(days: Int) {
        _periodDays.value = days
    }
}