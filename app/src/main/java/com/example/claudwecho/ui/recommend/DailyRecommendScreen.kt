package com.example.claudwecho.ui.recommend

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
import androidx.compose.material.icons.filled.Refresh
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
            Text("暂无推荐", color = Color.Gray)
        } else {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "每日推荐",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.wear.compose.material3.CompactButton(
                            onClick = { viewModel.loadData(forceRefresh = true) },
                            colors = androidx.wear.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D2D))
                        ) {
                            androidx.wear.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Refresh, 
                                contentDescription = "Refresh", 
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                items(songs.size, key = { songs[it].id }) { index ->
                    val song = songs[index]
                    com.example.claudwecho.ui.components.SharedSongItem(
                        song = song,
                        onClick = { onNavigateToPlayer(songs, index) }
                    )
                }
            }
        }
    }
}
