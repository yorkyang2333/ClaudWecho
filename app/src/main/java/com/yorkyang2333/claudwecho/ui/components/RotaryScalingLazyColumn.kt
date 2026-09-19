package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator

@Composable
fun rotaryContentPadding(
    bottomItemHeight: Dp = 52.dp,
    start: Dp = 8.dp,
    end: Dp = 8.dp,
    top: Dp = 0.dp
): PaddingValues {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val bottomPadding = (screenHeight - bottomItemHeight) / 2
    return PaddingValues(
        start = start,
        end = end,
        top = top,
        bottom = maxOf(0.dp, bottomPadding)
    )
}

@Composable
fun RotaryScalingLazyColumn(
    modifier: Modifier = Modifier,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    contentPadding: PaddingValues = rotaryContentPadding(),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(6.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    userScrollEnabled: Boolean = true,
    autoCentering: AutoCenteringParams? = null,
    isActivePage: Boolean = true,
    content: ScalingLazyListScope.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    var accumulatedRotaryPx by remember { mutableStateOf(0f) }
    var lastRotaryHapticTime by remember { mutableStateOf(0L) }
    
    ScreenScaffold(
        scrollState = state,
        scrollIndicator = {
            ScrollIndicator(
                state = state,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    ) {
        ScalingLazyColumn(
            modifier = modifier
                .onRotaryScrollEvent { event ->
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastRotaryHapticTime > 250L) {
                        accumulatedRotaryPx = 0f
                    }
                    accumulatedRotaryPx += event.verticalScrollPixels
                    if (Math.abs(accumulatedRotaryPx) >= 30f && currentTime - lastRotaryHapticTime >= 35L) {
                        view.performRotaryHaptic()
                        accumulatedRotaryPx = 0f
                        lastRotaryHapticTime = currentTime
                    }
                    false
                }
                .pointerInteropFilter { event ->
                    if (event.action == android.view.MotionEvent.ACTION_SCROLL) {
                        val vScroll = event.getAxisValue(android.view.MotionEvent.AXIS_VSCROLL)
                        if (vScroll != 0f) {
                            val scrollFactor = android.view.ViewConfiguration.get(view.context).scaledVerticalScrollFactor
                            val deltaPx = -vScroll * scrollFactor
                            
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastRotaryHapticTime > 250L) {
                                accumulatedRotaryPx = 0f
                            }
                            accumulatedRotaryPx += deltaPx
                            if (Math.abs(accumulatedRotaryPx) >= 30f && currentTime - lastRotaryHapticTime >= 35L) {
                                view.performRotaryHaptic()
                                accumulatedRotaryPx = 0f
                                lastRotaryHapticTime = currentTime
                            }
                            
                            coroutineScope.launch {
                                state.scrollBy(deltaPx)
                            }
                            true
                        } else false
                    } else false
                }
                .rotaryScrollable(RotaryScrollableDefaults.behavior(state), focusRequester)
                .focusRequester(focusRequester)
                .focusable(),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            userScrollEnabled = userScrollEnabled,
            autoCentering = autoCentering,
            content = content
        )
        
        LaunchedEffect(isActivePage) {
            if (isActivePage) {
                focusRequester.requestFocus()
            }
        }
    }
}
