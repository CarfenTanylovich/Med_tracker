package com.example.med_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.med_tracker.alarm.NotificationHelper
import com.example.med_tracker.di.SettingsPreferencesRepository
import com.example.med_tracker.presentation.cabinet.CabinetViewModel
import com.example.med_tracker.presentation.navigation.MedTrackerNavGraph
import com.example.med_tracker.presentation.navigation.Screen
import com.example.med_tracker.ui.theme.AppThemeMode
import com.example.med_tracker.ui.theme.Med_trackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MainAppScreen(
                settingsRepository = settingsRepository
            )
        }
    }
}

@Composable
fun MainAppScreen(
    settingsRepository: SettingsPreferencesRepository,
    cabinetViewModel: CabinetViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTheme by settingsRepository.themeMode.collectAsState(initial = AppThemeMode.AMOLED)

    Med_trackerTheme(themeMode = currentTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val screens = listOf(
            Screen.Cabinet,
            Screen.Today,
            Screen.History,
            Screen.Settings
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            MedTrackerNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                settingsRepository = settingsRepository,
                cabinetViewModel = cabinetViewModel,
                currentTheme = currentTheme,
                onThemeChange = { theme ->
                    coroutineScope.launch {
                        settingsRepository.saveThemeMode(theme)
                    }
                }
            )
        }
    }
}