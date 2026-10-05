package com.yorkyang2333.claudwecho.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val LocalScreenRound = staticCompositionLocalOf { false }

val ClaudWechoColorScheme = ColorScheme(
    primary = CoralPrimary,
    onPrimary = OnDarkCream,
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
    colorScheme: ColorScheme = ActiveDynamicTheme.colorScheme ?: ClaudWechoColorScheme,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val prefs = remember(context) { context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }
    val screenShapeSetting = remember(prefs) { prefs.getString("screen_shape", "auto") ?: "auto" }

    val isRound = remember(screenShapeSetting, config) {
        when (screenShapeSetting) {
            "round" -> true
            "square" -> false
            else -> config.isScreenRound
        }
    }

    CompositionLocalProvider(
        LocalScreenRound provides isRound
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
