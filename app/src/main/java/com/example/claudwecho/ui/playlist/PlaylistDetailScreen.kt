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
            Text("Loading...", color = MaterialTheme.colorScheme.primary)
        } else {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
            ) {
                items(songs.size, key = { songs[it].id }) { index ->
                    val song = songs[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = 4.dp)
                            .background(Color.DarkGray, RoundedCornerShape(8.dp))
                            .clickable {
                                onNavigateToPlayer(songs, index)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.al?.picUrl ?: "",
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.wear.compose.material3.Text(
                                    text = song.name, 
                                    style = MaterialTheme.typography.bodyLarge, 
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (song.fee == 1) {
                                    Text(
                                        text = "VIP",
                                        color = Color(0xFFFFD700),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 2.dp)
                                    )
                                }
                            }
                            Text(song.ar.firstOrNull()?.name ?: "Unknown Artist", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
