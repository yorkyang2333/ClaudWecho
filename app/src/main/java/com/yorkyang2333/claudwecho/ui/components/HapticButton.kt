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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

fun Modifier.onLongClickBeforeChild(enabled: Boolean = true, onLongClick: () -> Unit): Modifier = composed {
    if (!enabled) return@composed this
    val view = LocalView.current
    this.pointerInput(Unit) {
        coroutineScope {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val down = event.changes.firstOrNull { it.changedToDown() }
                    if (down != null) {
                        var isLongClick = false
                        val job = launch {
                            delay(viewConfiguration.longPressTimeoutMillis)
                            isLongClick = true
                            view.performClickHaptic()
                            onLongClick()
                        }
                        
                        val initialPosition = down.position
                        var allPointersUp = false
                        while (!allPointersUp) {
                            val nextEvent = awaitPointerEvent(PointerEventPass.Initial)
                            if (isLongClick) {
                                nextEvent.changes.forEach { it.consume() }
                            }
                            
                            val activePointers = nextEvent.changes.filter { it.pressed }
                            if (activePointers.isEmpty()) {
                                allPointersUp = true
                                job.cancel()
                            } else {
                                val change = activePointers.first()
                                val distance = (change.position - initialPosition).getDistance()
                                if (distance > viewConfiguration.touchSlop) {
                                    job.cancel()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
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
        modifier.onLongClickBeforeChild(enabled = enabled) {
            onLongClick()
        }
    } else {
        modifier
    }

    WearButton(
        onClick = {
            view.performClickHaptic()
            onClick()
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
        modifier.onLongClickBeforeChild(enabled = enabled) {
            onLongClick()
        }
    } else {
        modifier
    }

    WearButton(
        onClick = {
            view.performClickHaptic()
            onClick()
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
