package com.yorkyang2333.claudwecho.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.player.PlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun QueueScreen(
    viewModel: PlayerViewModel,
    onNavigateToPlayer: () -> Unit
) {
    val songs by viewModel.displayPlaylist.collectAsState()
    val currentIndex by viewModel.displayCurrentIndex.collectAsState()
    
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RotaryScalingLazyColumn(
            state = listState,
            autoCentering = null,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
            
            // Header Row (Trash, Count, Locate)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E))
                            .clickable { viewModel.clearQueue() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Clear Queue",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${songs.size}首",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E))
                            .clickable { 
                                coroutineScope.launch { 
                                    if (songs.isNotEmpty()) {
                                        // +2 because of the Spacer and Header Row
                                        listState.scrollToItem(currentIndex + 2)
                                    }
                                } 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Locate Current",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            if (songs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "暂无内容",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(songs.size, key = { index -> songs[index].id.toString() + index }) { index ->
                    val song = songs[index]
                    val isPlaying = index == currentIndex
                    // Could highlight the current playing song if we wanted to
                    com.yorkyang2333.claudwecho.ui.components.SharedSongItem(
                        song = song,
                        onClick = {
                            viewModel.playQueueItem(index)
                            onNavigateToPlayer()
                        }
                    )
                }
            }
        }
        
        com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "播放队列")
    }
}
