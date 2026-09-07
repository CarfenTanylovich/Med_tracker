package com.example.med_tracker.presentation.cabinet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.med_tracker.data.local.entity.MedicationEntity
import com.example.med_tracker.data.local.entity.ScheduleType
import com.example.med_tracker.presentation.common.MedicationFormIcon
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CabinetEditDialog(
    initialMedication: MedicationEntity? = null,
    initialTimes: List<String> = listOf("08:00"),
    initialScheduleType: ScheduleType = ScheduleType.DAYS_OF_WEEK,
    initialDaysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7),
    initialIntervalDays: Int = 2,
    initialCycleIntakeDays: Int = 2,
    initialCyclePauseDays: Int = 2,
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    val isEditMode = initialMedication != null

    var name by remember { mutableStateOf(initialMedication?.name ?: "") }
    var form by remember { mutableStateOf(initialMedication?.form ?: "Таблетка") }
    var dosage by remember { mutableStateOf(initialMedication?.dosage ?: "") }
    var quantityText by remember { mutableStateOf(initialMedication?.remainingQuantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialMedication?.unit ?: "") }

    val formsList = listOf("Таблетка", "Капсула", "Капли", "Укол")

    val times = remember {
        mutableStateListOf<String>().apply { addAll(initialTimes) }
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }

    var scheduleType by remember { mutableStateOf(initialScheduleType) }
    val selectedDays = remember {
        mutableStateListOf<Int>().apply {
            addAll(initialDaysOfWeek.ifEmpty { listOf(1, 2, 3, 4, 5, 6, 7) })
        }
    }

    var intervalText by remember { mutableStateOf(initialIntervalDays.toString()) }
    var cycleIntakeText by remember { mutableStateOf(initialCycleIntakeDays.toString()) }
    var cyclePauseText by remember { mutableStateOf(initialCyclePauseDays.toString()) }

    var notifyBeforeMinutes by remember { mutableIntStateOf(initialMedication?.notifyBeforeMinutes ?: 0) }
    var snoozeMinutes by remember { mutableIntStateOf(initialMedication?.snoozeMinutes ?: 10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Редактировать препарат" else "Новый препарат") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(54.dp)
                            .clickable {
                                val nextIndex = (formsList.indexOf(form) + 1) % formsList.size
                                form = formsList[nextIndex]
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MedicationFormIcon(
                                form = form,
                                size = 40.dp,
                                iconSize = 24.dp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Форма выпуска", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    formsList.forEach { formItem ->
                        FilterChip(
                            selected = form.equals(formItem, ignoreCase = true),
                            onClick = { form = formItem },
                            leadingIcon = {
                                MedicationFormIcon(
                                    form = formItem,
                                    iconSize = 14.dp
                                )
                            },
                            label = { Text(formItem) }
                        )
                    }
                }

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Дозировка") },
                    placeholder = { Text("100 мг, 1 таб, 2 капли") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Остаток") },
                        placeholder = { Text("Количество") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Ед. изм.") },
                        placeholder = { Text("шт, мл...") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text("Время приема", style = MaterialTheme.typography.titleSmall)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    times.forEachIndexed { index, t ->
                        InputChip(
                            selected = true,
                            onClick = {
                                editingTimeIndex = index
                                showTimePicker = true
                            },
                            label = { Text(t) },
                            trailingIcon = {
                                if (times.size > 1) {
                                    IconButton(
                                        onClick = {
                                            if (editingTimeIndex == index) editingTimeIndex = null
                                            times.removeAt(index)
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Удалить время")
                                    }
                                }
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            editingTimeIndex = null
                            showTimePicker = true
                        }
                    ) {
                        Text("+ Время")
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text("Уведомление до приема", style = MaterialTheme.typography.titleSmall)
                val notifyOptions = listOf(0 to "Точно", 5 to "За 5м", 10 to "За 10м", 15 to "За 15м", 30 to "За 30м")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    notifyOptions.forEach { (mins, label) ->
                        FilterChip(
                            selected = notifyBeforeMinutes == mins,
                            onClick = { notifyBeforeMinutes = mins },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text("Таймер повтора", style = MaterialTheme.typography.titleSmall)
                val snoozeOptions = listOf(10 to "10м", 15 to "15м", 30 to "30м", 60 to "1ч")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    snoozeOptions.forEach { (mins, label) ->
                        FilterChip(
                            selected = snoozeMinutes == mins,
                            onClick = { snoozeMinutes = mins },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text("Режим приема", style = MaterialTheme.typography.titleSmall)

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf(
                        ScheduleType.DAYS_OF_WEEK to "Дни",
                        ScheduleType.INTERVAL to "Интервал",
                        ScheduleType.CYCLE to "Цикл"
                    )
                    options.forEachIndexed { index, (type, label) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                            onClick = { scheduleType = type },
                            selected = scheduleType == type
                        ) {
                            Text(label)
                        }
                    }
                }

                when (scheduleType) {
                    ScheduleType.DAYS_OF_WEEK -> {
                        val daysMap = listOf(
                            1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт",
                            5 to "Пт", 6 to "Сб", 7 to "Вс"
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            daysMap.forEach { (dayNum, dayLabel) ->
                                val isSelected = selectedDays.contains(dayNum)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            if (selectedDays.size > 1) selectedDays.remove(dayNum)
                                        } else {
                                            selectedDays.add(dayNum)
                                        }
                                    },
                                    label = { Text(dayLabel) }
                                )
                            }
                        }
                    }

                    ScheduleType.INTERVAL -> {
                        OutlinedTextField(
                            value = intervalText,
                            onValueChange = { intervalText = it },
                            label = { Text("Каждые N дней (2 = через день)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    ScheduleType.CYCLE -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cycleIntakeText,
                                onValueChange = { cycleIntakeText = it },
                                label = { Text("Дней прием") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cyclePauseText,
                                onValueChange = { cyclePauseText = it },
                                label = { Text("Дней пауза") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 0
                    val interval = intervalText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val intakeDays = cycleIntakeText.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    val pauseDays = cyclePauseText.toIntOrNull()?.coerceAtLeast(0) ?: 0

                    if (name.isNotBlank()) {
                        onConfirm(
                            name, form, dosage, qty, unit,
                            times.toList(),
                            scheduleType,
                            selectedDays.toList(),
                            interval,
                            intakeDays,
                            pauseDays,
                            notifyBeforeMinutes,
                            snoozeMinutes
                        )
                    }
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    if (showTimePicker) {
        val targetIndex = editingTimeIndex
        val targetTimeString = targetIndex?.let { times.getOrNull(it) }
        val initialHour = targetTimeString?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
        val initialMinute = targetTimeString?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0

        DialTimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onDismiss = {
                showTimePicker = false
                editingTimeIndex = null
            },
            onConfirm = { hour, minute ->
                val formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                if (targetIndex != null && targetIndex in times.indices) {
                    times[targetIndex] = formatted
                } else if (!times.contains(formatted)) {
                    times.add(formatted)
                }
                times.sort()
                showTimePicker = false
                editingTimeIndex = null
            }
        )
    }
}