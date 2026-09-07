package com.example.med_tracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.med_tracker.di.SettingsPreferencesRepository
import com.example.med_tracker.presentation.cabinet.CabinetScreen
import com.example.med_tracker.presentation.cabinet.CabinetViewModel
import com.example.med_tracker.presentation.history.HistoryScreen
import com.example.med_tracker.presentation.history.HistoryViewModel
import com.example.med_tracker.presentation.settings.SettingsScreen
import com.example.med_tracker.presentation.today.TodayScreen
import com.example.med_tracker.presentation.today.TodayViewModel
import com.example.med_tracker.ui.theme.AppThemeMode
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Central navigation graph for the app.
 * Replaces inline NavHost setup in MainActivity for better separation of concerns.
 */
@Composable
fun MedTrackerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Cabinet.route,
    settingsRepository: SettingsPreferencesRepository,
    cabinetViewModel: CabinetViewModel = hiltViewModel(),
    currentTheme: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Cabinet.route) {
            CabinetScreen(
                viewModel = cabinetViewModel
            )
        }
        composable(Screen.Today.route) {
            val todayViewModel: TodayViewModel = hiltViewModel()
            TodayScreen(viewModel = todayViewModel)
        }
        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(viewModel = historyViewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                settingsRepository = settingsRepository
            )
        }
    }
}
