package com.yorkyang2333.claudwecho.theme

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import com.materialkolor.hct.Hct
import com.materialkolor.quantize.QuantizerCelebi
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.score.Score

object ActiveDynamicTheme {
    var colorScheme by mutableStateOf<ColorScheme?>(null)
}

/**
 * Extracts a dominant representative seed color int from a given bitmap using Monet algorithm.
 */
fun extractSeedColorFromBitmap(bitmap: Bitmap): Int? {
    return try {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val quantizerResult = QuantizerCelebi.quantize(pixels, 128)
        Score.score(quantizerResult).firstOrNull()
    } catch (_: Exception) {
        null
    }
}

/**
 * Generates a full Wear OS Material 3 ColorScheme based on a seed color int.
 * Uses SchemeContent for high fidelity to album artwork colors in dark mode.
 */
fun generateWearColorSchemeFromSeed(seedColorInt: Int, fallbackColor: ColorScheme = ClaudWechoColorScheme): ColorScheme {
    return try {
        val hctColor = Hct.fromInt(seedColorInt)
        val dynamicScheme = SchemeContent(hctColor, true, 0.0)

        ColorScheme(
            primary = Color(dynamicScheme.primary),
            onPrimary = Color(dynamicScheme.onPrimary),
            primaryDim = Color(dynamicScheme.primaryFixedDim),
            primaryContainer = Color(dynamicScheme.primaryContainer),
            onPrimaryContainer = Color(dynamicScheme.onPrimaryContainer),
            secondary = Color(dynamicScheme.secondary),
            secondaryDim = Color(dynamicScheme.secondaryFixedDim),
            onSecondary = Color(dynamicScheme.onSecondary),
            secondaryContainer = Color(dynamicScheme.secondaryContainer),
            onSecondaryContainer = Color(dynamicScheme.onSecondaryContainer),
            tertiary = Color(dynamicScheme.tertiary),
            tertiaryDim = Color(dynamicScheme.tertiaryFixedDim),
            onTertiary = Color(dynamicScheme.onTertiary),
            tertiaryContainer = Color(dynamicScheme.tertiaryContainer),
            onTertiaryContainer = Color(dynamicScheme.onTertiaryContainer),
            background = fallbackColor.background,
            onBackground = Color(dynamicScheme.onBackground),
            onSurface = Color(dynamicScheme.onSurface),
            onSurfaceVariant = Color(dynamicScheme.onSurfaceVariant),
            surfaceContainerLow = Color(dynamicScheme.surfaceContainerLow),
            surfaceContainer = Color(dynamicScheme.surfaceContainer),
            surfaceContainerHigh = Color(dynamicScheme.surfaceContainerHigh),
            error = Color(dynamicScheme.error),
            onError = Color(dynamicScheme.onError)
        )
    } catch (_: Exception) {
        fallbackColor
    }
}
