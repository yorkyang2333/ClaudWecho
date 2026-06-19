package com.example.claudwecho.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayerScreen(viewModel: PlayerViewModel) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTitle by viewModel.currentTrackTitle.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (currentTitle == null) {
            Text(
                text = "暂无播放内容",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = currentTitle ?: "No track selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val repeatMode by viewModel.repeatMode.collectAsState()
                    val shuffleMode by viewModel.shuffleModeEnabled.collectAsState()
                    
                    val repeatText = when (repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_ONE -> "单曲"
                        androidx.media3.common.Player.REPEAT_MODE_ALL -> "列表"
                        else -> "顺序"
                    }
                    
                    Button(
                        onClick = { viewModel.toggleRepeatMode() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    ) {
                        Text(repeatText, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = { viewModel.toggleShuffleMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (shuffleMode) MaterialTheme.colorScheme.primary else Color.DarkGray
                        ),
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    ) {
                        Text("随机", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.skipToPrevious() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    ) {
                        Text("|<")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.playOrPause() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                    ) {
                        Text(if (isPlaying) "||" else ">")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.skipToNext() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    ) {
                        Text(">|")
                    }
                }
            }
        }
    }
}
