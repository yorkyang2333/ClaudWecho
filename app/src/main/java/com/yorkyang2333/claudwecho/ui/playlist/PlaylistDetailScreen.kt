package com.yorkyang2333.claudwecho.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    type: String = "playlist",
    viewModel: PlaylistDetailViewModel = koinViewModel(),
    onNavigateToPlayer: (List<com.yorkyang2333.claudwecho.data.api.Song>, Int) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val title by viewModel.title.collectAsState()

    LaunchedEffect(playlistId, type) {
        when (type) {
            "playlist" -> viewModel.loadPlaylist(playlistId)
            "album" -> viewModel.loadAlbum(playlistId)
            "djradio" -> viewModel.loadDjRadio(playlistId)
            "liked" -> viewModel.loadLiked()
            else -> viewModel.loadPlaylist(playlistId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLazyColumn(
            scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.3f,
                minTransitionArea = 0.4f
            ),
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
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
                    title = title ?: when (type) {
                        "liked" -> "我喜欢的音乐"
                        "playlist" -> "歌单"
                        "album" -> "专辑"
                        "djradio" -> "博客"
                        else -> "音乐列表"
                    },
                    actionIcon = if (type == "liked") {
                        {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color(0xFF2D2D2D))
                                    .clickable { viewModel.loadLiked(forceRefresh = true) },
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
                    } else null
                )
            }
        }
    }
}
