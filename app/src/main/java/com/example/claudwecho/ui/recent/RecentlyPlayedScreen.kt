package com.example.claudwecho.ui.recent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage

@Composable
fun RecentlyPlayedScreen(
    viewModel: RecentlyPlayedViewModel,
    onNavigateToPlayer: (List<com.example.claudwecho.data.api.Song>, Int) -> Unit
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
        } else if (songs.isEmpty()) {
            Text("暂无播放记录", color = Color.Gray)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLazyColumn(
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                    items(songs.size, key = { songs[it].id }) { index ->
                        val song = songs[index]
                        com.example.claudwecho.ui.components.SharedSongItem(
                            song = song,
                            onClick = { onNavigateToPlayer(songs, index) }
                        )
                    }
                }
                
                com.example.claudwecho.ui.components.PinnedHeader(
                    title = "最近播放",
                    actionIcon = {
                        androidx.wear.compose.material3.CompactButton(
                            onClick = { viewModel.loadData(forceRefresh = true) },
                            colors = androidx.wear.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D2D))
                        ) {
                            androidx.wear.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.Refresh, 
                                contentDescription = "Refresh", 
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        }
    }
}
