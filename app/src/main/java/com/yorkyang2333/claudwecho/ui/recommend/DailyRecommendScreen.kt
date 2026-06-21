package com.yorkyang2333.claudwecho.ui.recommend

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
fun DailyRecommendScreen(
    viewModel: DailyRecommendViewModel,
    onNavigateToPlayer: (List<com.yorkyang2333.claudwecho.data.api.Song>, Int) -> Unit
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
            androidx.wear.compose.material.CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else if (songs.isEmpty()) {
            Text("暂无推荐", color = Color.Gray)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLazyColumn(
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                    items(songs.size, key = { songs[it].id }) { index ->
                        val song = songs[index]
                        com.yorkyang2333.claudwecho.ui.components.SharedSongItem(
                            song = song,
                            onClick = { onNavigateToPlayer(songs, index) }
                        )
                    }
                }
                
                com.yorkyang2333.claudwecho.ui.components.PinnedHeader(
                    title = "每日推荐",
                    actionIcon = {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFF2D2D2D))
                                .clickable { viewModel.loadData(forceRefresh = true) },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.wear.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.Refresh, 
                                contentDescription = "Refresh", 
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        }
    }
}
