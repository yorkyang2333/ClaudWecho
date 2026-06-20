package com.yorkyang2333.claudwecho.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val ClaudWechoColorScheme = ColorScheme(
    primary = CoralPrimary,
    onPrimary = OnDarkCream, // typically white, using OnDarkCream
    primaryContainer = CoralPrimaryActive,
    onPrimaryContainer = OnDarkCream,
    secondary = AccentTeal,
    onSecondary = InkDark,
    background = SurfaceDark,
    onBackground = OnDarkCream,
    surfaceContainer = SurfaceDarkElevated,
    onSurface = OnDarkCream,
    onSurfaceVariant = OnDarkSoft,
    error = ErrorRed,
    onError = OnDarkCream
)

@Composable
fun ClaudWechoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClaudWechoColorScheme,
        typography = Typography,
        content = content
    )
}
