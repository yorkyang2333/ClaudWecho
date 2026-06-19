package com.example.claudwecho.ui.player

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.wear.compose.material3.Text

@Composable
fun PlayerMenuScreen(
    viewModel: PlayerViewModel
) {
    val context = LocalContext.current
    val shuffleMode by viewModel.shuffleModeEnabled.collectAsState()
    
    ScalingLazyColumn(
        autoCentering = androidx.wear.compose.foundation.lazy.AutoCenteringParams(itemIndex = 1),
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
    ) {
        item {
            Text("播放设置", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 16.dp))
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text("调节音量", color = Color.White) },
                icon = { Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color.White) }
            )
        }
        item {
            Button(
                onClick = {
                    if (!shuffleMode) {
                        viewModel.toggleShuffleMode()
                    } else {
                        viewModel.toggleShuffleMode()
                        viewModel.toggleRepeatMode()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text(if (shuffleMode) "随机播放" else "列表循环", color = Color.White) },
                icon = { Icon(if (shuffleMode) Icons.Filled.Shuffle else Icons.Filled.Repeat, null, tint = Color.White) }
            )
        }
    }
}
