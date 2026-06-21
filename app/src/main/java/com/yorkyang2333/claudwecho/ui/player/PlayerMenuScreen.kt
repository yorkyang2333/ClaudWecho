package com.yorkyang2333.claudwecho.ui.player

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun PlayerMenuScreen(
    viewModel: PlayerViewModel
) {
    val context = LocalContext.current
    val shuffleMode by viewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isFmMode by viewModel.isPersonalFmMode.collectAsState()
    
    val playbackModeText = when {
        shuffleMode -> "随机播放"
        repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE -> "单曲循环"
        repeatMode == androidx.media3.common.Player.REPEAT_MODE_ALL -> "列表循环"
        else -> "播完即止"
    }

    val playbackModeIcon = when {
        shuffleMode -> Icons.Rounded.Shuffle
        repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
        repeatMode == androidx.media3.common.Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
        else -> Icons.AutoMirrored.Rounded.ArrowForward
    }
    
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
            item {
                Button(
                    onClick = {
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_SAME,
                            AudioManager.FLAG_SHOW_UI
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { 
                        Text(
                            text = "调节音量",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        ) 
                    },
                    icon = { Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            
            if (!isFmMode) {
                item {
                    Button(
                        onClick = {
                            viewModel.cyclePlaybackMode()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        label = { 
                            Text(
                                text = playbackModeText,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            ) 
                        },
                        icon = { Icon(playbackModeIcon, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }
        }
        
        com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "播放设置")
    }
}
