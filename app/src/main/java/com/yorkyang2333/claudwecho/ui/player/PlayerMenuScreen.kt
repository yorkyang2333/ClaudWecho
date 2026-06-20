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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
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
    
    ScalingLazyColumn(
            scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.3f,
                minTransitionArea = 0.4f
            ),
        autoCentering = null,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
    ) {
        item {
            Text(
                text = "播放设置", 
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text("调节音量") },
                icon = { Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = MaterialTheme.colorScheme.primary) }
            )
        }
        
        if (!isFmMode) {
            item {
                Button(
                    onClick = {
                        viewModel.cyclePlaybackMode()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { Text(playbackModeText) },
                    icon = { Icon(playbackModeIcon, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
        }
    }
}
