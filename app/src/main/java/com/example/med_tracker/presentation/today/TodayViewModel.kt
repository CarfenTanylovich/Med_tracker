package com.example.med_tracker.presentation.today

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.data.repository.MedicationRepository
import com.example.med_tracker.di.SettingsPreferencesRepository
import com.example.med_tracker.domain.model.TodayIntakeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: MedicationRepository,
    private val settingsRepository: SettingsPreferencesRepository,
    val application: Application
) : ViewModel() {

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

    val showQuantityInIntakes: StateFlow<Boolean> = settingsRepository.showQuantityInIntakes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    init {
        generateLogs()
    }

    fun generateLogs() {
        viewModelScope.launch {
            repository.generateLogsAhead(application, daysAhead = 14)
        }
    }

    fun updateIntake(item: TodayIntakeItem, newStatus: IntakeStatus, actualTimeMillis: Long?) {
        viewModelScope.launch {
            repository.updateIntakeStatus(application, item, newStatus, actualTimeMillis)
        }
    }
}