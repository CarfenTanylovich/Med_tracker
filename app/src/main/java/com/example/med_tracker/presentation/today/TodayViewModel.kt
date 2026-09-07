package com.example.med_tracker.presentation.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.data.repository.MedicationRepository
import com.example.med_tracker.domain.model.TodayIntakeItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MedicationRepository(AppDatabase.getInstance(application))

    val upcomingIntakes: StateFlow<List<TodayIntakeItem>> = repository.getUpcomingIntakes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pastIntakes: StateFlow<List<TodayIntakeItem>> = repository.getPastIntakes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        generateLogs()
    }

    fun generateLogs() {
        viewModelScope.launch {
            repository.generateLogsAhead(getApplication(), daysAhead = 14)
        }
    }

    fun updateIntake(item: TodayIntakeItem, newStatus: IntakeStatus, actualTimeMillis: Long?) {
        viewModelScope.launch {
            repository.updateIntakeStatus(getApplication(), item, newStatus, actualTimeMillis)
        }
    }
}