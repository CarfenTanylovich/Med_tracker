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
import androidx.core.view.WindowInsetsControllerCompat

enum class AppThemeMode(val title: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
    AMOLED("Тёмная AMOLED")
}

// Fallback colors для устройств с API < 31
private val FallbackDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8)
)

private val FallbackLightColorScheme = lightColorScheme(
    primary = Color(0xFF825500),
    secondary = Color(0xFFA1755F),
    tertiary = Color(0xFFFDCA74)
)

/**
 * Converts a dark color scheme to AMOLED mode by setting the background
 * to pure black (0xFF000000) for OLED/AMOLED displays.
 */
private fun ColorScheme.toAmoled(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color.Black,
    surfaceContainerHighest = Color.Black,
    surfaceContainerHigh = Color.Black,
    surfaceContainer = Color.Black,
    surfaceContainerLow = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceDim = Color.Black,
    surfaceBright = Color.Black.copy(alpha = 0.95f),
)

/**
 * Main theme composable that applies the selected theme mode and configures
 * system bars (status bar & navigation bar) colors.
 */
@Composable
fun Med_trackerTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.AMOLED, AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val systemInDark = darkTheme
    val baseDark = FallbackDarkColorScheme
    val baseLight = FallbackLightColorScheme

    val context = LocalContext.current
    val colorScheme = when {
        // Динамические цвета на API 31+
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            when (themeMode) {
                AppThemeMode.AMOLED, AppThemeMode.DARK -> dynamicDarkColorScheme(context)
                AppThemeMode.LIGHT -> dynamicLightColorScheme(context)
                AppThemeMode.SYSTEM -> if (systemInDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
        }
        // Fallback для API < 31
        else -> {
            when (themeMode) {
                AppThemeMode.AMOLED -> baseDark.toAmoled()
                AppThemeMode.DARK -> baseDark
                AppThemeMode.LIGHT -> baseLight
                AppThemeMode.SYSTEM -> if (systemInDark) baseDark else baseLight
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        @Suppress("DEPRECATION")
        SideEffect {
            val isLight = themeMode == AppThemeMode.LIGHT || (themeMode == AppThemeMode.SYSTEM && !systemInDark)
            val window = (context as Activity).window
            WindowInsetsControllerCompat(window, view).apply {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                isAppearanceLightStatusBars = isLight
                isAppearanceLightNavigationBars = isLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
