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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = koinViewModel(),
    onBack: () -> Unit,
    url: String,
    title: String
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTitle by viewModel.currentTrackTitle.collectAsState()

    val lyrics by viewModel.lyrics.collectAsState()
    val currentLyricIndex by viewModel.currentLyricIndex.collectAsState()
    val listState = rememberScalingLazyListState()

    androidx.compose.runtime.LaunchedEffect(url, title) {
        if (url.isNotEmpty()) {
            viewModel.playSong(url, title)
        }
    }

    androidx.compose.runtime.LaunchedEffect(currentLyricIndex) {
        if (currentLyricIndex >= 0 && currentLyricIndex < lyrics.size) {
            // Scroll to current lyric (offset by 1 because first item is the player UI)
            listState.animateScrollToItem(currentLyricIndex + 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentTitle ?: "No track selected",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

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
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (lyrics.isEmpty()) {
                item {
                    Text(
                        text = "No lyrics available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                itemsIndexed(lyrics) { index, line ->
                    val isCurrent = index == currentLyricIndex
                    Text(
                        text = line.text,
                        style = if (isCurrent) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
