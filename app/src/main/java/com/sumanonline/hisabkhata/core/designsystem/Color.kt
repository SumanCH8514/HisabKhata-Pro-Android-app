package com.sumanonline.hisabkhata.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Default static constants for theme initializers & non-composable contexts
val DefaultBrandBlue = Color(0xFF0057BB)
val DefaultBrandBlueDark = Color(0xFF004494)
val DefaultBrandBlueLight = Color(0xFFEFF6FF)
val DefaultPageBackground = Color(0xFFF5F7F9)
val DefaultSurfaceWhite = Color(0xFFFFFFFF)
val DefaultSurfaceCard = Color(0xFFFFFFFF)
val DefaultTextPrimary = Color(0xFF0F172A)
val DefaultTextSecondary = Color(0xFF475569)
val DefaultTextTertiary = Color(0xFF94A3B8)
val DefaultTextMuted = Color(0xFFCBD5E1)
val DefaultBorderLight = Color(0xFFE2E8F0)
val DefaultBorderMedium = Color(0xFFCBD5E1)

// Dynamic theme-aware tokens that automatically react to current theme & accent color
val BrandBlue: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val BrandBlueDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val BrandBlueLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

val BrandAccentOrange = Color(0xFFFFA500)

val GiveRed = Color(0xFFDC2626)
val GiveRedLight = Color(0xFFFEF2F2)
val GiveRedStroke = Color(0xFFF4C7C3)
val GiveRedDark = Color(0xFF991B1B)

val GotGreen = Color(0xFF16A34A)
val GotGreenLight = Color(0xFFF0FDF4)
val GotGreenStroke = Color(0xFFB7E1CD)
val GotGreenDark = Color(0xFF166534)

val PageBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val SurfaceWhite: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val SurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val BorderLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val BorderMedium: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val WarningAmber = Color(0xFFD97706)
val WarningAmberLight = Color(0xFFFFFBEB)
val InfoSky = Color(0xFF0284C7)
val InfoSkyLight = Color(0xFFF0F9FF)

val NavyBluePrimary = Color(0xFF073B7E)
val NavyBlueDark = Color(0xFF002A5C)
val NavyBlueLightPill = Color(0xFFDBEAFE)
val RedProBadge = Color(0xFFDC2626)
val GrayPillBg = Color(0xFFF1F5F9)
val RedPillBg = Color(0xFFFEE2E2)
val GreenPillBg = Color(0xFFDCFCE7)
