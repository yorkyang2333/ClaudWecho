package com.yorkyang2333.claudwecho.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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

@OptIn(ExperimentalFoundationApi::class)
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
    onLongClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    
    val buttonModifier = if (onLongClick != null) {
        modifier.combinedClickable(
            interactionSource = actualInteractionSource,
            indication = null,
            enabled = enabled,
            onLongClick = {
                view.performClickHaptic()
                onLongClick()
            },
            onClick = {
                view.performClickHaptic()
                onClick()
            }
        )
    } else {
        modifier
    }

    WearButton(
        onClick = if (onLongClick != null) { {} } else {
            {
                view.performClickHaptic()
                onClick()
            }
        },
        modifier = buttonModifier,
        enabled = enabled,
        colors = colors,
        interactionSource = actualInteractionSource,
        shape = shape,
        border = border,
        contentPadding = contentPadding,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
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
    onLongClick: (() -> Unit)? = null,
    icon: @Composable (androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    secondaryLabel: @Composable (RowScope.() -> Unit)? = null,
    label: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    val buttonModifier = if (onLongClick != null) {
        modifier.combinedClickable(
            interactionSource = actualInteractionSource,
            indication = null,
            enabled = enabled,
            onLongClick = {
                view.performClickHaptic()
                onLongClick()
            },
            onClick = {
                view.performClickHaptic()
                onClick()
            }
        )
    } else {
        modifier
    }

    WearButton(
        onClick = if (onLongClick != null) { {} } else {
            {
                view.performClickHaptic()
                onClick()
            }
        },
        modifier = buttonModifier,
        enabled = enabled,
        colors = colors,
        interactionSource = actualInteractionSource,
        shape = shape,
        border = border,
        contentPadding = contentPadding,
        label = label,
        secondaryLabel = secondaryLabel,
        icon = icon
    )
}
