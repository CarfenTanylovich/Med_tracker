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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.med_tracker.di.SettingsPreferencesRepository
import com.example.med_tracker.presentation.cabinet.MedicationSortOrder
import com.example.med_tracker.presentation.common.PermissionsDialog
import com.example.med_tracker.presentation.common.SortSelectionDialog
import com.example.med_tracker.ui.theme.AppThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    settingsRepository: SettingsPreferencesRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val sortOrder by settingsRepository.sortOrder.collectAsState(initial = MedicationSortOrder.NAME_ASC)
    val showQuantityInIntakes by settingsRepository.showQuantityInIntakes.collectAsState(initial = true)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }) }
    ) { innerPadding ->
        Column(
            modifier = modifier
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

            // Показать количество в приемах
            SettingSwitchItem(
                title = "Показывать количество в приемах",
                checked = showQuantityInIntakes,
                onCheckedChange = { checked ->
                    coroutineScope.launch {
                        settingsRepository.saveShowQuantityInIntakes(checked)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Внешний вид",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Тема
            SettingClickableItem(
                title = "Тема",
                subtitle = currentTheme.title,
                onClick = { showThemeDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Разрешения",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Разрешения
            SettingClickableItem(
                title = "Управление разрешениями",
                subtitle = "Управление разрешениями приложения",
                onClick = { showPermissionsDialog = true }
            )
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onSelect = { theme ->
                onThemeChange(theme)
                showThemeDialog = false
            }
        )
    }

    if (showSortDialog) {
        SortSelectionDialog(
            currentSort = sortOrder,
            onDismiss = { showSortDialog = false },
            onSelect = { order ->
                coroutineScope.launch {
                    settingsRepository.saveSortOrder(order)
                }
                showSortDialog = false
            }
        )
    }

    if (showPermissionsDialog) {
        PermissionsDialog(
            onDismiss = { showPermissionsDialog = false }
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: AppThemeMode,
    onDismiss: () -> Unit,
    onSelect: (AppThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите тему") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppThemeMode.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onSelect(theme) }
                        )
                        Text(
                            text = theme.title,
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

@Composable
private fun SettingClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}