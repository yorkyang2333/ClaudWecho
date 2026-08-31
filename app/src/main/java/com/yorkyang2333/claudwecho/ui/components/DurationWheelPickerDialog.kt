package com.yorkyang2333.claudwecho.ui.components

import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.launch

@Composable
fun DurationWheelPickerDialog(
    showDialog: Boolean,
    initialMinutes: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    if (!showDialog) return

    val initHours = (initialMinutes / 60).coerceIn(0, 23)
    val initMins = (initialMinutes % 60).coerceIn(0, 59)

    val hoursList = remember { (0..23).toList() }
    val minutesList = remember { (0..59).toList() }

    val hoursState = rememberScalingLazyListState(initialCenterItemIndex = initHours)
    val minutesState = rememberScalingLazyListState(initialCenterItemIndex = initMins)

    var focusedColumn by remember { mutableStateOf(1) } // 0: hours, 1: minutes

    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }

    var accumulatedRotaryPx by remember { mutableStateOf(0f) }
    var lastRotaryHapticTime by remember { mutableStateOf(0L) }

    LaunchedEffect(showDialog) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onRotaryScrollEvent { event ->
                    val currentState = if (focusedColumn == 0) hoursState else minutesState
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastRotaryHapticTime > 250L) {
                        accumulatedRotaryPx = 0f
                    }
                    accumulatedRotaryPx += event.verticalScrollPixels
                    if (Math.abs(accumulatedRotaryPx) >= 28f && currentTime - lastRotaryHapticTime >= 35L) {
                        view.performRotaryHaptic()
                        accumulatedRotaryPx = 0f
                        lastRotaryHapticTime = currentTime
                    }
                    coroutineScope.launch {
                        currentState.scrollBy(event.verticalScrollPixels)
                    }
                    true
                }
                .pointerInteropFilter { event ->
                    if (event.action == MotionEvent.ACTION_SCROLL) {
                        val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                        if (vScroll != 0f) {
                            val currentState = if (focusedColumn == 0) hoursState else minutesState
                            val scrollFactor = ViewConfiguration.get(view.context).scaledVerticalScrollFactor
                            val deltaPx = -vScroll * scrollFactor
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastRotaryHapticTime > 250L) {
                                accumulatedRotaryPx = 0f
                            }
                            accumulatedRotaryPx += deltaPx
                            if (Math.abs(accumulatedRotaryPx) >= 28f && currentTime - lastRotaryHapticTime >= 35L) {
                                view.performRotaryHaptic()
                                accumulatedRotaryPx = 0f
                                lastRotaryHapticTime = currentTime
                            }
                            coroutineScope.launch {
                                currentState.scrollBy(deltaPx)
                            }
                            true
                        } else false
                    } else false
                }
                .rotaryScrollable(
                    RotaryScrollableDefaults.behavior(if (focusedColumn == 0) hoursState else minutesState),
                    focusRequester
                )
                .focusRequester(focusRequester)
                .focusable(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Title / Hint
                Text(
                    text = "设置时长",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Pickers Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Center Selection Indicator Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2E).copy(alpha = 0.55f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours Column
                        WheelColumn(
                            items = hoursList,
                            state = hoursState,
                            isFocused = (focusedColumn == 0),
                            unitLabel = "时",
                            modifier = Modifier
                                .weight(1f)
                                .height(105.dp)
                                .clickable { focusedColumn = 0 }
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Minutes Column
                        WheelColumn(
                            items = minutesList,
                            state = minutesState,
                            isFocused = (focusedColumn == 1),
                            unitLabel = "分",
                            modifier = Modifier
                                .weight(1f)
                                .height(105.dp)
                                .clickable { focusedColumn = 1 }
                        )
                    }
                }

                // Action Buttons at bottom
                DialogActionButtons(
                    onCancel = onDismissRequest,
                    onConfirm = {
                        val selectedHours = hoursState.centerItemIndex.coerceIn(0, 23)
                        val selectedMins = minutesState.centerItemIndex.coerceIn(0, 59)
                        val totalMinutes = selectedHours * 60 + selectedMins
                        val finalDuration = if (totalMinutes <= 0) 1 else totalMinutes
                        onConfirm(finalDuration)
                    },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun WheelColumn(
    items: List<Int>,
    state: ScalingLazyListState,
    isFocused: Boolean,
    unitLabel: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isFocused) {
                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 0.dp),
            autoCentering = AutoCenteringParams(itemIndex = 0)
        ) {
            items(items.size) { index ->
                val num = items[index]
                val isSelected by remember {
                    derivedStateOf { state.centerItemIndex == index }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clickable {
                            coroutineScope.launch {
                                state.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format("%02d", num),
                            style = if (isSelected) {
                                MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            },
                            color = if (isSelected) {
                                if (isFocused) MaterialTheme.colorScheme.primary else Color.White
                            } else {
                                Color.Gray.copy(alpha = 0.5f)
                            }
                        )
                        if (isSelected) {
                            Text(
                                text = unitLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.LightGray,
                                modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

