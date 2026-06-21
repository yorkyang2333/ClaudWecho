package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold

@Composable
fun RotaryScalingLazyColumn(
    modifier: Modifier = Modifier,
    state: ScalingLazyListState = rememberScalingLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    userScrollEnabled: Boolean = true,
    autoCentering: AutoCenteringParams? = AutoCenteringParams(),
    content: ScalingLazyListScope.() -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = state)
        }
    ) {
        ScalingLazyColumn(
            modifier = modifier
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
        
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
