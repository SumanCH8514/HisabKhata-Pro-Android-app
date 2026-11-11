package com.sumanonline.hisabkhata.core.designsystem

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandBlueDark,
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

@Composable
fun HisabKhataTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BrandBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
