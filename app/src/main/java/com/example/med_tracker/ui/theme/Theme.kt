package com.example.med_tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(val title: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
    AMOLED("Тёмная AMOLED")
}

private val FallbackDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8)
)

private val FallbackLightColorScheme = lightColorScheme(
    primary = Color(0xFF6650A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260)
)

private fun ColorScheme.toAmoled(): ColorScheme {
    return this.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceDim = Color.Black,
        surfaceBright = Color(0xFF121212),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF0A0A0A),
        surfaceContainer = Color(0xFF121212),
        surfaceContainerHigh = Color(0xFF1A1A1A),
        surfaceContainerHighest = Color(0xFF222222),
        surfaceVariant = Color(0xFF161616)
    )
}

@Composable
fun Med_trackerTheme(
    themeMode: AppThemeMode = AppThemeMode.AMOLED,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val baseDark = if (supportsDynamic) dynamicDarkColorScheme(context) else FallbackDarkColorScheme
    val baseLight = if (supportsDynamic) dynamicLightColorScheme(context) else FallbackLightColorScheme

    val colorScheme = when (themeMode) {
        AppThemeMode.AMOLED -> baseDark.toAmoled()
        AppThemeMode.DARK -> baseDark
        AppThemeMode.LIGHT -> baseLight
        AppThemeMode.SYSTEM -> if (systemInDark) baseDark else baseLight
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val isLight = themeMode == AppThemeMode.LIGHT || (themeMode == AppThemeMode.SYSTEM && !systemInDark)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}