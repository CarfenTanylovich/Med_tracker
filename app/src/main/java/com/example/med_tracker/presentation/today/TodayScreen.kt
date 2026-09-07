package com.example.med_tracker.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.med_tracker.data.local.entity.IntakeStatus
import com.example.med_tracker.domain.model.TodayIntakeItem
import com.example.med_tracker.presentation.common.MedicationFormIcon
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = viewModel()
) {
    val upcoming by viewModel.upcomingIntakes.collectAsState()
    val past by viewModel.pastIntakes.collectAsState()

    val tabs = listOf("Текущие", "Прошедшие")
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    var selectedItemForEdit by remember { mutableStateOf<TodayIntakeItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedFilterDateMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Панель поиска и календарь
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Поиск") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(28.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Выбрать дату",
                    tint = if (selectedFilterDateMillis != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Чип активного фильтра даты
        if (selectedFilterDateMillis != null) {
            val filterDateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            val formattedDate = filterDateFormat.format(Date(selectedFilterDateMillis!!))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InputChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Дата: $formattedDate") },
                    trailingIcon = {
                        IconButton(
                            onClick = { selectedFilterDateMillis = null },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Сбросить дату")
                        }
                    }
                )
            }
        }

        // Вкладки
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (pagerState.currentPage == index) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )
            }
        }

        // Свайпы между вкладками
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val sourceList = if (pageIndex == 0) upcoming else past

            val filteredList = sourceList.filter { item ->
                val matchesQuery = item.name.contains(searchQuery, ignoreCase = true)
                val matchesDate = if (selectedFilterDateMillis != null) {
                    isSameCalendarDay(item.scheduledTimeMillis, selectedFilterDateMillis!!)
                } else {
                    true
                }
                matchesQuery && matchesDate
            }

            val dateHeaderFormat = SimpleDateFormat("d MMMM, EE", Locale("ru"))
            val grouped = filteredList.groupBy {
                dateHeaderFormat.format(Date(it.scheduledTimeMillis))
            }

            if (grouped.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (pageIndex == 0) "Нет запланированных приёмов" else "История приёмов пуста",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    grouped.forEach { (dateHeader, items) ->
                        item {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(items, key = { it.logId }) { intake ->
                            IntakeRowCard(
                                item = intake,
                                onClick = { selectedItemForEdit = intake }
                            )
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedFilterDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }) {
                        Text("Выбрать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Отмена")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        selectedItemForEdit?.let { item ->
            EditIntakeDialog(
                item = item,
                onDismiss = { selectedItemForEdit = null },
                onSave = { newStatus, actualTime ->
                    viewModel.updateIntake(item, newStatus, actualTime)
                    selectedItemForEdit = null
                }
            )
        }
    }
}

private fun isSameCalendarDay(itemMillis: Long, pickerUtcMillis: Long): Boolean {
    val calItem = Calendar.getInstance().apply { timeInMillis = itemMillis }
    val calPicker = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = pickerUtcMillis }

    return calItem.get(Calendar.YEAR) == calPicker.get(Calendar.YEAR) &&
            calItem.get(Calendar.MONTH) == calPicker.get(Calendar.MONTH) &&
            calItem.get(Calendar.DAY_OF_MONTH) == calPicker.get(Calendar.DAY_OF_MONTH)
}

@Composable
fun IntakeRowCard(
    item: TodayIntakeItem,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(item.scheduledTimeMillis))

    val isMissed = item.status == IntakeStatus.MISSED
    val cardColor = if (isMissed) {
        Color(0xFF5A1E1E)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                MedicationFormIcon(
                    form = item.form,
                    size = 40.dp,
                    iconSize = 22.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.form}: ${item.dosage} (${item.remainingQuantity} ${item.unit})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = timeString,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EditIntakeDialog(
    item: TodayIntakeItem,
    onDismiss: () -> Unit,
    onSave: (IntakeStatus, Long?) -> Unit
) {
    val dateFormat = SimpleDateFormat("d MMMM yyyy 'г.'", Locale("ru"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val dateString = dateFormat.format(Date(item.scheduledTimeMillis))
    val scheduledTimeString = timeFormat.format(Date(item.scheduledTimeMillis))
    val actualTimeString = item.actualTimeMillis?.let { timeFormat.format(Date(it)) } ?: scheduledTimeString

    var status by remember { mutableStateOf(item.status) }

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
                    OutlinedTextField(
                        value = actualTimeString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("По факту") },
                        modifier = Modifier.weight(1f)
                    )
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
            Button(
                onClick = {
                    val actualTime = if (status == IntakeStatus.TAKEN) (item.actualTimeMillis ?: System.currentTimeMillis()) else null
                    onSave(status, actualTime)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отменить")
            }
        }
    )
}