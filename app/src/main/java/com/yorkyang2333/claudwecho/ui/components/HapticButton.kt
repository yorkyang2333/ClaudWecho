package com.yorkyang2333.claudwecho.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Button as WearButton

fun View.performRotaryHaptic() {
    if (!performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)) {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}

fun View.performClickHaptic() {
    if (!performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)) {
        if (!performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}

fun Modifier.hapticClickable(
    interactionSource: MutableInteractionSource? = null,
    indication: androidx.compose.foundation.Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: androidx.compose.ui.semantics.Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = actualInteractionSource,
        indication = indication ?: androidx.compose.foundation.LocalIndication.current,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = {
            view.performClickHaptic()
            onClick()
        }
    )
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = ButtonDefaults.shape,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    WearButton(
        onClick = {
            view.performClickHaptic()
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        shape = shape,
        border = border,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = ButtonDefaults.shape,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    icon: @Composable (androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    secondaryLabel: @Composable (RowScope.() -> Unit)? = null,
    label: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    WearButton(
        onClick = {
            view.performClickHaptic()
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        shape = shape,
        border = border,
        contentPadding = contentPadding,
        label = label,
        secondaryLabel = secondaryLabel,
        icon = icon
    )
}
