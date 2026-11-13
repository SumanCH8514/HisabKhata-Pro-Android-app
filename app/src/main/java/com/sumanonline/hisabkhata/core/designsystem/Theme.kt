package com.sumanonline.hisabkhata.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun getAccentColor(name: String): Color {
    return when (name) {
        "Emerald Green" -> Color(0xFF16A34A)
        "Royal Purple" -> Color(0xFF7C3AED)
        "Amber Gold" -> Color(0xFFD97706)
        "Crimson Red" -> Color(0xFFDC2626)
        "Teal Ocean" -> Color(0xFF0D9488)
        else -> DefaultBrandBlue
    }
}

@Composable
fun HisabKhataTheme(
    themeMode: String = "System",
    accentColor: String = "Brand Blue",
    fontSize: String = "Standard",
    fontFamily: String = "System Default",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }
    val primaryColor = getAccentColor(accentColor)
    val primaryLight = primaryColor.copy(alpha = 0.12f)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF1E293B),
            onPrimaryContainer = Color(0xFFF8FAFC),
            secondary = GotGreen,
            onSecondary = Color.White,
            secondaryContainer = GotGreenDark,
            onSecondaryContainer = Color(0xFFDCFCE7),
            tertiary = GiveRed,
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFF7F1D1D),
            onTertiaryContainer = Color(0xFFFEE2E2),
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF1E293B),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF334155),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF475569),
            outlineVariant = Color(0xFF64748B)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryLight,
            onPrimaryContainer = primaryColor,
            secondary = GotGreen,
            onSecondary = Color.White,
            secondaryContainer = DefaultGotGreenLight,
            onSecondaryContainer = GotGreenDark,
            tertiary = GiveRed,
            onTertiary = Color.White,
            tertiaryContainer = DefaultGiveRedLight,
            onTertiaryContainer = GiveRedDark,
            background = DefaultPageBackground,
            onBackground = DefaultTextPrimary,
            surface = DefaultSurfaceWhite,
            onSurface = DefaultTextPrimary,
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = DefaultTextSecondary,
            outline = DefaultBorderLight,
            outlineVariant = DefaultBorderMedium
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (isDark) Color(0xFF0F172A).toArgb() else primaryColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            window.navigationBarColor = if (isDark) Color(0xFF0F172A).toArgb() else Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    val dynamicTypography = getAppTypography(fontFamily, fontSize)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}
