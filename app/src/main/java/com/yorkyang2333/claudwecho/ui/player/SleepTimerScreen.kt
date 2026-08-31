package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.components.DurationWheelPickerDialog
import com.yorkyang2333.claudwecho.ui.components.PinnedHeader
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn

@Composable
fun SleepTimerScreen(
    viewModel: PlayerViewModel
) {
    val sleepTimerManager = viewModel.sleepTimerManager
    val isEnabled by sleepTimerManager.isEnabled.collectAsState()
    val durationMinutes by sleepTimerManager.durationMinutes.collectAsState()
    val finishCurrentSong by sleepTimerManager.finishCurrentSong.collectAsState()
    val remainingSeconds by sleepTimerManager.remainingSeconds.collectAsState()
    val isWaitingForSongEnd by sleepTimerManager.isWaitingForSongEnd.collectAsState()

    var showWheelDialog by remember { mutableStateOf(false) }

    val formattedDuration = if (durationMinutes >= 60) {
        val hours = durationMinutes / 60
        val mins = durationMinutes % 60
        if (mins == 0) "$hours 小时" else "$hours 小时 $mins 分钟"
    } else {
        "$durationMinutes 分钟"
    }

    val remainingText = if (isEnabled) {
        if (isWaitingForSongEnd) {
            "等待播完当前歌曲"
        } else {
            val rMins = remainingSeconds / 60
            val rSecs = remainingSeconds % 60
            "剩余 ${String.format("%02d:%02d", rMins, rSecs)}"
        }
    } else null

    Box(modifier = Modifier.fillMaxSize()) {
        RotaryScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }

            // Card 1: 启用
            item {
                TimerCard(
                    onClick = { sleepTimerManager.setEnabled(!isEnabled) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "启用",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        CustomSwitch(
                            checked = isEnabled,
                            onCheckedChange = { sleepTimerManager.setEnabled(it) }
                        )
                    }
                }
            }

            // Card 2: 时长
            item {
                TimerCard(
                    onClick = { showWheelDialog = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "时长",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (remainingText != null) {
                                Text(
                                    text = remainingText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = formattedDuration,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Card 3: 播完当前歌曲
            item {
                TimerCard(
                    onClick = { sleepTimerManager.setFinishCurrentSong(!finishCurrentSong) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "播完当前歌曲",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        CustomSwitch(
                            checked = finishCurrentSong,
                            onCheckedChange = { sleepTimerManager.setFinishCurrentSong(it) }
                        )
                    }
                }
            }

            // Description hint text below Card 3
            item {
                Text(
                    text = "倒计时结束后，等待当前歌曲播放完再暂停；期间手动暂停或切歌会取消定时关闭",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    color = Color(0xFF8E8E93),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        PinnedHeader(title = "定时关闭")

        DurationWheelPickerDialog(
            showDialog = showWheelDialog,
            initialMinutes = durationMinutes,
            onDismissRequest = { showWheelDialog = false },
            onConfirm = { newMinutes ->
                sleepTimerManager.setDuration(newMinutes)
                showWheelDialog = false
            }
        )
    }
}

@Composable
private fun TimerCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1E))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color(0xFF3A3A3C),
        animationSpec = tween(durationMillis = 200)
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

