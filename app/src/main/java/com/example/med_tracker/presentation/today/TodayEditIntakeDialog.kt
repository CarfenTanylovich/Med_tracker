package com.example.med_tracker.presentation.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.domain.model.TodayIntakeItem
import com.example.med_tracker.presentation.cabinet.DialTimePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayEditIntakeDialog(
    item: TodayIntakeItem,
    onDismiss: () -> Unit,
    onSave: (IntakeStatus, Long?) -> Unit
) {
    val dateFormat = SimpleDateFormat("d MMMM yyyy 'г.'", Locale("ru"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val dateString = dateFormat.format(Date(item.scheduledTimeMillis))
    val scheduledTimeString = timeFormat.format(Date(item.scheduledTimeMillis))

    var status by remember { mutableStateOf(item.status) }
    var actualTimeMillis by remember {
        mutableLongStateOf(item.actualTimeMillis ?: System.currentTimeMillis())
    }
    var showTimePicker by remember { mutableStateOf(false) }

    val actualTimeString = timeFormat.format(Date(actualTimeMillis))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Изменить") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateString,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Дата") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scheduledTimeString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("По расписанию") },
                        modifier = Modifier.weight(1f)
                    )

                    // Обертка с прозрачным перехватом клика для открытия таймпикера
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = actualTimeString,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            label = { Text("По факту") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showTimePicker = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { status = IntakeStatus.MISSED },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == IntakeStatus.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (status == IntakeStatus.MISSED) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Не принято")
                    }

                    Button(
                        onClick = { status = IntakeStatus.TAKEN },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status == IntakeStatus.TAKEN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (status == IntakeStatus.TAKEN) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Принято")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val finalActualTime = if (status == IntakeStatus.TAKEN) actualTimeMillis else null
                onSave(status, finalActualTime)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = actualTimeMillis }
        DialTimePickerDialog(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                val updatedCalendar = Calendar.getInstance().apply {
                    timeInMillis = item.scheduledTimeMillis
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                actualTimeMillis = updatedCalendar.timeInMillis
                showTimePicker = false
            }
        )
    }
}