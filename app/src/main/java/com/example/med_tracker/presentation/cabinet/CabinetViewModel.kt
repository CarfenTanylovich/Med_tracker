package com.example.med_tracker.presentation.cabinet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.med_tracker.data.local.AppDatabase
import com.example.med_tracker.data.local.entity.MedicationEntity
import com.example.med_tracker.data.local.entity.ScheduleType
import com.example.med_tracker.data.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MedicationSortOrder(val title: String) {
    NAME_ASC("По названию (от А до Я)"),
    NAME_DESC("По названию (от Я до А)"),
    QUANTITY_ASC("По остатку (сначала меньше)"),
    QUANTITY_DESC("По остатку (сначала больше)")
}

class CabinetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MedicationRepository(AppDatabase.getInstance(application))

    val sortOrder = MutableStateFlow(MedicationSortOrder.NAME_ASC)

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

    fun setSortOrder(order: MedicationSortOrder) {
        sortOrder.value = order
    }

    fun addMedication(
        name: String,
        form: String,
        dosage: String,
        quantity: Int,
        unit: String,
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
            val entity = MedicationEntity(
                name = name,
                form = form,
                dosage = dosage,
                remainingQuantity = quantity,
                unit = unit,
                notifyBeforeMinutes = notifyBeforeMinutes,
                snoozeMinutes = snoozeMinutes
            )
            repository.addMedicationWithSchedules(
                medication = entity,
                times = times,
                scheduleType = scheduleType,
                daysOfWeek = daysOfWeek,
                intervalDays = intervalDays,
                cycleIntakeDays = cycleIntakeDays,
                cyclePauseDays = cyclePauseDays,
                context = getApplication()
            )
        }
    }

    fun updateMedicationFull(
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
                context = getApplication()
            )
        }
    }

    suspend fun getScheduleTimes(medicationId: Long): List<String> {
        return repository.getScheduleTimesForMedication(medicationId)
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun updateMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.updateMedication(medication)
        }
    }
}