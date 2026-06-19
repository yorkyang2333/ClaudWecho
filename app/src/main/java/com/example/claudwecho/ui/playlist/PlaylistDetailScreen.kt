package com.example.claudwecho.ui.playlist

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
import androidx.compose.material.icons.filled.Refresh
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    type: String = "playlist",
    viewModel: PlaylistDetailViewModel = koinViewModel(),
    onNavigateToPlayer: (List<com.example.claudwecho.data.api.Song>, Int) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val songs by viewModel.songs.collectAsState()

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
            ScalingLazyColumn(
        autoCentering = androidx.wear.compose.foundation.lazy.AutoCenteringParams(itemIndex = 1),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.wear.compose.material3.Text(
                            text = when (type) {
                                "liked" -> "我喜欢的音乐"
                                "playlist" -> "歌单"
                                "album" -> "专辑"
                                "djradio" -> "播客"
                                else -> "音乐列表"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (type == "liked") {
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.wear.compose.material3.IconButton(
                                onClick = { viewModel.loadLiked() },
                                modifier = Modifier.size(36.dp),
                                colors = androidx.wear.compose.material3.IconButtonDefaults.filledTonalIconButtonColors()
                            ) {
                                androidx.wear.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh, 
                                    contentDescription = "Refresh", 
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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
