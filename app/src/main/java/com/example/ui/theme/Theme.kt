package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CbtColorScheme = darkColorScheme(
    primary = AccentCyanBright,
    onPrimary = PrimaryNavy,
    primaryContainer = PrimaryNavyLight,
    onPrimaryContainer = TextPrimary,
    secondary = AccentTeal,
    onSecondary = TextPrimary,
    background = DarkCanvas,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    error = SecurityRed,
    onError = TextPrimary,
    errorContainer = SecurityRedBg,
    onErrorContainer = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CbtColorScheme,
        typography = Typography,
        content = content
    )
}
