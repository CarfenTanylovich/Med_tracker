package com.example.med_tracker.presentation.cabinet

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.med_tracker.data.local.entity.MedicationEntity
import com.example.med_tracker.data.local.entity.ScheduleEntity
import com.example.med_tracker.data.local.entity.ScheduleType
import com.example.med_tracker.data.repository.MedicationRepository
import com.example.med_tracker.di.SettingsPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MedicationSortOrder(val title: String) {
    NAME_ASC("По названию (от А до Я)"),
    NAME_DESC("По названию (от Я до А)"),
    QUANTITY_ASC("По остатку (сначала меньше)"),
    QUANTITY_DESC("По остатку (сначала больше)")
}

@HiltViewModel
class CabinetViewModel @Inject constructor(
    private val application: Application,
    private val repository: MedicationRepository,
    private val settingsRepository: SettingsPreferencesRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(MedicationSortOrder.NAME_ASC)
    val sortOrder: StateFlow<MedicationSortOrder> = _sortOrder

    init {
        viewModelScope.launch {
            settingsRepository.sortOrder.collect { order ->
                _sortOrder.value = order
            }
        }
    }

    val medications: StateFlow<List<MedicationEntity>> = repository.allMedications
        .combine(sortOrder) { list, order ->
            when (order) {
                MedicationSortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                MedicationSortOrder.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                MedicationSortOrder.QUANTITY_ASC -> list.sortedBy { it.remainingQuantity }
                MedicationSortOrder.QUANTITY_DESC -> list.sortedByDescending { it.remainingQuantity }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun getScheduleData(medicationId: Long): Pair<List<String>, ScheduleEntity?> {
        val schedules = repository.getSchedulesForMedication(medicationId)
        val times = schedules.map { it.time }.ifEmpty { listOf("08:00") }
        return Pair(times, schedules.firstOrNull())
    }

    fun addMedication(
        name: String,
        form: String,
        dosage: String,
        unit: String,
        initialQuantity: Int,
        times: List<String>,
        scheduleType: ScheduleType,
        daysOfWeek: List<Int>,
        intervalDays: Int,
        cycleIntakeDays: Int,
        cyclePauseDays: Int,
        notifyBeforeMinutes: Int,
        snoozeMinutes: Int
    ) {
        viewModelScope.launch {
            val medication = MedicationEntity(
                name = name,
                form = form,
                dosage = dosage,
                unit = unit,
                remainingQuantity = initialQuantity,
                notifyBeforeMinutes = notifyBeforeMinutes,
                snoozeMinutes = snoozeMinutes
            )
            repository.addMedicationWithSchedules(
                medication = medication,
                times = times,
                scheduleType = scheduleType,
                daysOfWeek = daysOfWeek,
                intervalDays = intervalDays,
                cycleIntakeDays = cycleIntakeDays,
                cyclePauseDays = cyclePauseDays,
                context = application
            )
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication, application)
        }
    }

    fun updateMedication(
        medication: MedicationEntity,
        times: List<String>,
        scheduleType: ScheduleType,
        daysOfWeek: List<Int>,
        intervalDays: Int,
        cycleIntakeDays: Int,
        cyclePauseDays: Int
    ) {
        viewModelScope.launch {
            repository.updateMedicationWithSchedules(
                medication = medication,
                times = times,
                scheduleType = scheduleType,
                daysOfWeek = daysOfWeek,
                intervalDays = intervalDays,
                cycleIntakeDays = cycleIntakeDays,
                cyclePauseDays = cyclePauseDays,
                context = application
            )
        }
    }

    fun updateQuantity(medicationId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateMedicationQuantity(medicationId, quantity)
        }
    }

    fun updateSortOrder(order: MedicationSortOrder) {
        viewModelScope.launch {
            settingsRepository.saveSortOrder(order)
        }
    }
}