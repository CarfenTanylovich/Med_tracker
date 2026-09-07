package com.example.med_tracker.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.med_tracker.presentation.cabinet.CabinetViewModel
import com.example.med_tracker.presentation.cabinet.MedicationSortOrder
import com.example.med_tracker.presentation.cabinet.SortSelectionDialog
import com.example.med_tracker.presentation.common.PermissionsDialog
import com.example.med_tracker.ui.theme.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    cabinetViewModel: CabinetViewModel = viewModel()
) {
    val sortOrder by cabinetViewModel.sortOrder.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }

    var showQuantityInIntakes by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Основные настройки",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Сортировка лекарств
            SettingClickableItem(
                title = "Сортировка лекарств",
                subtitle = sortOrder.title,
                onClick = { showSortDialog = true }
            )

            // Тема приложения
            SettingClickableItem(
                title = "Тема приложения",
                subtitle = currentTheme.title,
                onClick = { showThemeDialog = true }
            )

            // Системные разрешения
            SettingClickableItem(
                title = "Разрешения и фоновая работа",
                subtitle = "Настройка точных алертов и работы от батареи",
                onClick = { showPermissionsDialog = true }
            )

            // Переключатель остатка
            SettingSwitchItem(
                title = "Отображать остаток лекарства в приемах",
                checked = showQuantityInIntakes,
                onCheckedChange = { showQuantityInIntakes = it }
            )
        }

        if (showSortDialog) {
            SortSelectionDialog(
                currentSort = sortOrder,
                onDismiss = { showSortDialog = false },
                onSelect = {
                    cabinetViewModel.setSortOrder(it)
                    showSortDialog = false
                }
            )
        }

        if (showThemeDialog) {
            ThemeDialog(
                currentTheme = currentTheme,
                onDismiss = { showThemeDialog = false },
                onSelect = {
                    onThemeChange(it)
                    showThemeDialog = false
                }
            )
        }

        if (showPermissionsDialog) {
            PermissionsDialog(onDismiss = { showPermissionsDialog = false })
        }
    }
}

@Composable
private fun SettingClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeDialog(
    currentTheme: AppThemeMode,
    onDismiss: () -> Unit,
    onSelect: (AppThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Тема приложения") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppThemeMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == mode,
                            onClick = { onSelect(mode) }
                        )
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}