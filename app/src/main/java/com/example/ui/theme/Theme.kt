package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadioDarkColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = Color(0xFF1E1500),
    primaryContainer = Color(0xFF3B2A05),
    onPrimaryContainer = AmberGoldLight,
    secondary = ProfileHisAccent,
    onSecondary = Color(0xFF001B2B),
    secondaryContainer = Color(0xFF0C2D44),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = ProfileHerAccent,
    onTertiary = Color(0xFF2B0014),
    tertiaryContainer = Color(0xFF4A1028),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = RadioDarkBg,
    onBackground = TextPrimaryDark,
    surface = RadioSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = RadioCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = RadioCardBorder,
    outlineVariant = Color(0xFF1E293B),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RadioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
