package com.example.med_tracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Cabinet : Screen("cabinet", "Лекарства", Icons.AutoMirrored.Filled.List)
    object Today : Screen("today", "Приём", Icons.Default.DateRange)
    object History : Screen("history", "История", Icons.Default.Info)
    object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}
