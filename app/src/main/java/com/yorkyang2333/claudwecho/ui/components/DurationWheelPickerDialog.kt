package com.yorkyang2333.claudwecho.ui.components

import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.rememberPickerState
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
    val initHours = (initialMinutes / 60).coerceIn(0, 23)
    val initMins = (initialMinutes % 60).coerceIn(0, 59)

    val hoursState = rememberPickerState(
        initialNumberOfOptions = 24,
        initiallySelectedOption = initHours,
        repeatItems = false
    )
    val minutesState = rememberPickerState(
        initialNumberOfOptions = 60,
        initiallySelectedOption = initMins,
        repeatItems = false
    )

    var focusedColumn by remember { mutableStateOf(1) } // 0: hours, 1: minutes

    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }

    var accumulatedRotaryDelta by remember { mutableStateOf(0f) }
    var lastRotaryTime by remember { mutableStateOf(0L) }

    // Auto switch focus when user touches or scrolls a picker
    LaunchedEffect(hoursState.isScrollInProgress) {
        if (hoursState.isScrollInProgress) {
            focusedColumn = 0
        }
    }

    LaunchedEffect(minutesState.isScrollInProgress) {
        if (minutesState.isScrollInProgress) {
            focusedColumn = 1
        }
    }

    // Haptic vibration whenever selected option changes
    var lastHoursOption by remember { mutableStateOf(initHours) }
    var lastMinutesOption by remember { mutableStateOf(initMins) }

    LaunchedEffect(hoursState.selectedOption) {
        if (hoursState.selectedOption != lastHoursOption) {
            view.performRotaryHaptic()
            lastHoursOption = hoursState.selectedOption
        }
    }

    LaunchedEffect(minutesState.selectedOption) {
        if (minutesState.selectedOption != lastMinutesOption) {
            view.performRotaryHaptic()
            lastMinutesOption = minutesState.selectedOption
        }
    }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            focusRequester.requestFocus()
        }
    }

    val itemHeight = 40.dp

    Dialog(
        showDialog = showDialog,
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onRotaryScrollEvent { event ->
                    val now = System.currentTimeMillis()
                    if (now - lastRotaryTime > 250L) {
                        accumulatedRotaryDelta = 0f
                    }
                    accumulatedRotaryDelta += event.verticalScrollPixels
                    lastRotaryTime = now

                    val threshold = 26f
                    if (Math.abs(accumulatedRotaryDelta) >= threshold) {
                        val steps = (accumulatedRotaryDelta / threshold).toInt()
                        accumulatedRotaryDelta -= steps * threshold
                        coroutineScope.launch {
                            val activeState = if (focusedColumn == 0) hoursState else minutesState
                            val target = (activeState.selectedOption + steps).coerceIn(0, activeState.numberOfOptions - 1)
                            activeState.animateScrollToOption(target)
                        }
                    }
                    true
                }
                .pointerInteropFilter { event ->
                    if (event.action == MotionEvent.ACTION_SCROLL) {
                        val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                        if (vScroll != 0f) {
                            val scrollFactor = ViewConfiguration.get(view.context).scaledVerticalScrollFactor
                            val deltaPx = -vScroll * scrollFactor
                            val now = System.currentTimeMillis()
                            if (now - lastRotaryTime > 250L) {
                                accumulatedRotaryDelta = 0f
                            }
                            accumulatedRotaryDelta += deltaPx
                            lastRotaryTime = now

                            val threshold = 26f
                            if (Math.abs(accumulatedRotaryDelta) >= threshold) {
                                val steps = (accumulatedRotaryDelta / threshold).toInt()
                                accumulatedRotaryDelta -= steps * threshold
                                coroutineScope.launch {
                                    val activeState = if (focusedColumn == 0) hoursState else minutesState
                                    val target = (activeState.selectedOption + steps).coerceIn(0, activeState.numberOfOptions - 1)
                                    activeState.animateScrollToOption(target)
                                }
                            }
                            true
                        } else false
                    } else false
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(44.dp))

                // Pickers Area: Designed to fit within standard Wear OS safe insets
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Center Selection Indicator Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2C2C2E).copy(alpha = 0.55f))
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hours Picker Column: Fills left half
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (focusedColumn == 0) {
                                        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    } else Modifier
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        focusedColumn = 0
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Picker(
                                state = hoursState,
                                contentDescription = "小时",
                                modifier = Modifier.fillMaxSize(),
                                separation = 0.dp
                            ) { optionIndex ->
                                val isSelected = (hoursState.selectedOption == optionIndex)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(itemHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d", optionIndex),
                                            style = if (isSelected) {
                                                MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                            color = if (isSelected) {
                                                if (focusedColumn == 0) MaterialTheme.colorScheme.primary else Color.White
                                            } else {
                                                Color.Gray.copy(alpha = 0.35f)
                                            }
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = "时",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = if (focusedColumn == 0) MaterialTheme.colorScheme.primary else Color.LightGray,
                                                modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        // Minutes Picker Column: Fills right half
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .then(
                                    if (focusedColumn == 1) {
                                        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                    } else Modifier
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        focusedColumn = 1
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Picker(
                                state = minutesState,
                                contentDescription = "分钟",
                                modifier = Modifier.fillMaxSize(),
                                separation = 0.dp
                            ) { optionIndex ->
                                val isSelected = (minutesState.selectedOption == optionIndex)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(itemHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d", optionIndex),
                                            style = if (isSelected) {
                                                MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                MaterialTheme.typography.titleMedium.copy(
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            },
                                            color = if (isSelected) {
                                                if (focusedColumn == 1) MaterialTheme.colorScheme.primary else Color.White
                                            } else {
                                                Color.Gray.copy(alpha = 0.35f)
                                            }
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = "分",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = if (focusedColumn == 1) MaterialTheme.colorScheme.primary else Color.LightGray,
                                                modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons: Standard Wear OS Material 3 pattern
                DialogActionButtons(
                    onCancel = onDismissRequest,
                    onConfirm = {
                        val totalMinutes = hoursState.selectedOption * 60 + minutesState.selectedOption
                        val finalDuration = if (totalMinutes <= 0) 1 else totalMinutes
                        onConfirm(finalDuration)
                    },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            PinnedHeader(
                title = "设置时长",
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
