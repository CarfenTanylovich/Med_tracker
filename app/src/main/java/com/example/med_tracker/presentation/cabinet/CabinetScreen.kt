package com.example.med_tracker.presentation.cabinet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.med_tracker.data.local.entity.MedicationEntity
import com.example.med_tracker.data.local.entity.ScheduleEntity
import com.example.med_tracker.data.local.entity.ScheduleType
import com.example.med_tracker.presentation.common.SortSelectionDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CabinetScreen(
    modifier: Modifier = Modifier,
    onMedicationClick: (MedicationEntity) -> Unit = {},
    viewModel: CabinetViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val medications by viewModel.medications.collectAsState()
    val currentSort by viewModel.sortOrder.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    var editingTimes by remember { mutableStateOf<List<String>>(listOf("08:00")) }
    var editingSchedule by remember { mutableStateOf<ScheduleEntity?>(null) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Лекарства") },
                actions = {
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Сортировка")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить лекарство")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (medications.isEmpty()) {
                Text(
                    text = "Список лекарств пуст",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(medications, key = { it.id }) { medication ->
                        MedicationCard(
                            medication = medication,
                            onClick = {
                                coroutineScope.launch {
                                    val (times, schedule) = viewModel.getScheduleData(medication.id)
                                    editingTimes = times
                                    editingSchedule = schedule
                                    editingMedication = medication
                                }
                                onMedicationClick(medication)
                            },
                            onDeleteClick = { viewModel.deleteMedication(medication) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CabinetEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, form, dosage, quantity, unit, times, scheduleType, daysOfWeek, intervalDays, cycleIntakeDays, cyclePauseDays, notifyBeforeMinutes, snoozeMinutes ->
                viewModel.addMedication(
                    name = name,
                    form = form,
                    dosage = dosage,
                    unit = unit,
                    initialQuantity = quantity,
                    times = times,
                    scheduleType = scheduleType,
                    daysOfWeek = daysOfWeek,
                    intervalDays = intervalDays,
                    cycleIntakeDays = cycleIntakeDays,
                    cyclePauseDays = cyclePauseDays,
                    notifyBeforeMinutes = notifyBeforeMinutes,
                    snoozeMinutes = snoozeMinutes
                )
                showAddDialog = false
            }
        )
    }

    if (editingMedication != null) {
        CabinetEditDialog(
            initialMedication = editingMedication,
            initialTimes = editingTimes,
            initialScheduleType = editingSchedule?.scheduleType ?: ScheduleType.DAYS_OF_WEEK,
            initialDaysOfWeek = editingSchedule?.daysOfWeek ?: listOf(1, 2, 3, 4, 5, 6, 7),
            initialIntervalDays = editingSchedule?.intervalDays ?: 2,
            initialCycleIntakeDays = editingSchedule?.cycleIntakeDays ?: 2,
            initialCyclePauseDays = editingSchedule?.cyclePauseDays ?: 2,
            onDismiss = {
                editingMedication = null
                editingSchedule = null
            },
            onConfirm = { name, form, dosage, quantity, unit, times, scheduleType, daysOfWeek, intervalDays, cycleIntakeDays, cyclePauseDays, notifyBeforeMinutes, snoozeMinutes ->
                editingMedication?.let { med ->
                    viewModel.updateMedication(
                        medication = med.copy(
                            name = name,
                            form = form,
                            dosage = dosage,
                            unit = unit,
                            remainingQuantity = quantity,
                            notifyBeforeMinutes = notifyBeforeMinutes,
                            snoozeMinutes = snoozeMinutes
                        ),
                        times = times,
                        scheduleType = scheduleType,
                        daysOfWeek = daysOfWeek,
                        intervalDays = intervalDays,
                        cycleIntakeDays = cycleIntakeDays,
                        cyclePauseDays = cyclePauseDays
                    )
                }
                editingMedication = null
                editingSchedule = null
            }
        )
    }

    if (showSortDialog) {
        SortSelectionDialog(
            currentSort = currentSort,
            onDismiss = { showSortDialog = false },
            onSelect = { sortOrder ->
                viewModel.updateSortOrder(sortOrder)
            }
        )
    }
}