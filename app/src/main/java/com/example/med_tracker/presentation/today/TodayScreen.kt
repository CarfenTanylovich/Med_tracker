package com.example.med_tracker.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.derivedStateOf
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
import com.example.med_tracker.domain.model.TodayIntakeItem
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
    val showQuantity by viewModel.showQuantityInIntakes.collectAsState()

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
            .statusBarsPadding()
    ) {
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

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val sourceList = if (pageIndex == 0) upcoming else past

            val filteredItems by remember(sourceList, searchQuery, selectedFilterDateMillis) {
                derivedStateOf {
                    sourceList.filter { item ->
                        val matchesQuery = item.name.contains(searchQuery, ignoreCase = true)
                        val matchesDate = if (selectedFilterDateMillis != null) {
                            isSameCalendarDay(item.scheduledTimeMillis, selectedFilterDateMillis!!)
                        } else {
                            true
                        }
                        matchesQuery && matchesDate
                    }
                }
            }

            val dateHeaderFormat = SimpleDateFormat("d MMMM, EE", Locale("ru"))

            if (filteredItems.isEmpty()) {
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
                val flatList = remember(filteredItems, dateHeaderFormat) {
                    buildFlatList(filteredItems, dateHeaderFormat)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(flatList, key = { _, flatItem -> flatItem.uniqueKey }) { index, flatItem ->
                        if (index == 0 || flatItem.header != flatList[index - 1].header) {
                            Text(
                                text = flatItem.header,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        TodayIntakeCard(
                            item = flatItem.item,
                            showQuantity = showQuantity,
                            onClick = { selectedItemForEdit = flatItem.item }
                        )
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
            TodayEditIntakeDialog(
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

private data class FlatListItem(
    val item: TodayIntakeItem,
    val header: String
) {
    val uniqueKey: String = "header_${header}_item_${item.logId}"
}

private fun buildFlatList(
    filteredItems: List<TodayIntakeItem>,
    dateHeaderFormat: SimpleDateFormat
): List<FlatListItem> {
    val flatList = mutableListOf<FlatListItem>()
    var currentDateHeader: String? = null

    filteredItems.forEach { item ->
        val header = dateHeaderFormat.format(Date(item.scheduledTimeMillis))
        if (header != currentDateHeader) {
            currentDateHeader = header
        }
        flatList.add(FlatListItem(item = item, header = header))
    }

    return flatList
}

private fun isSameCalendarDay(itemMillis: Long, pickerUtcMillis: Long): Boolean {
    val calItem = Calendar.getInstance().apply { timeInMillis = itemMillis }
    val calPicker = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = pickerUtcMillis }

    return calItem.get(Calendar.YEAR) == calPicker.get(Calendar.YEAR) &&
            calItem.get(Calendar.MONTH) == calPicker.get(Calendar.MONTH) &&
            calItem.get(Calendar.DAY_OF_MONTH) == calPicker.get(Calendar.DAY_OF_MONTH)
}