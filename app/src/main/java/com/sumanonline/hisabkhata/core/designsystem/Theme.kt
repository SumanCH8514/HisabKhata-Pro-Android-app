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
        else -> BrandBlue
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
    val primaryDark = primaryColor.copy(alpha = 0.8f)
    val primaryLight = primaryColor.copy(alpha = 0.12f)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = SurfaceWhite,
            primaryContainer = primaryDark,
            onPrimaryContainer = SurfaceWhite,
            secondary = GotGreen,
            onSecondary = SurfaceWhite,
            secondaryContainer = GotGreenDark,
            onSecondaryContainer = GotGreenLight,
            tertiary = GiveRed,
            onTertiary = SurfaceWhite,
            background = Color(0xFF111827),
            onBackground = Color(0xFFF9FAFB),
            surface = Color(0xFF1F2937),
            onSurface = Color(0xFFF9FAFB),
            surfaceVariant = Color(0xFF374151),
            onSurfaceVariant = Color(0xFF9CA3AF),
            outline = Color(0xFF4B5563),
            outlineVariant = Color(0xFF374151)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = SurfaceWhite,
            primaryContainer = primaryLight,
            onPrimaryContainer = primaryColor,
            secondary = GotGreen,
            onSecondary = SurfaceWhite,
            secondaryContainer = GotGreenLight,
            onSecondaryContainer = GotGreenDark,
            tertiary = GiveRed,
            onTertiary = SurfaceWhite,
            tertiaryContainer = GiveRedLight,
            onTertiaryContainer = GiveRedDark,
            background = PageBackground,
            onBackground = TextPrimary,
            surface = SurfaceWhite,
            onSurface = TextPrimary,
            surfaceVariant = PageBackground,
            onSurfaceVariant = TextSecondary,
            outline = BorderLight,
            outlineVariant = BorderMedium
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (isDark) Color(0xFF111827).toArgb() else primaryColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val dynamicTypography = getAppTypography(fontFamily, fontSize)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}
