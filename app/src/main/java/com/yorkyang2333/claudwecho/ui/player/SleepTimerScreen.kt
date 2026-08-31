package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.components.Button
import com.yorkyang2333.claudwecho.ui.components.CustomSwitch
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

            // Button 1: 启用
            item {
                Button(
                    onClick = { sleepTimerManager.setEnabled(!isEnabled) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "启用",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            CustomSwitch(
                                checked = isEnabled,
                                onCheckedChange = { sleepTimerManager.setEnabled(it) }
                            )
                        }
                    }
                )
            }

            // Button 2: 时长
            item {
                Button(
                    onClick = { showWheelDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "时长",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formattedDuration,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    secondaryLabel = if (remainingText != null) {
                        {
                            Text(
                                text = remainingText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else null
                )
            }

            // Button 3: 播完当前歌曲
            item {
                Button(
                    onClick = { sleepTimerManager.setFinishCurrentSong(!finishCurrentSong) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "播完当前歌曲",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            CustomSwitch(
                                checked = finishCurrentSong,
                                onCheckedChange = { sleepTimerManager.setFinishCurrentSong(it) }
                            )
                        }
                    }
                )
            }

            // Description hint text below Button 3
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
