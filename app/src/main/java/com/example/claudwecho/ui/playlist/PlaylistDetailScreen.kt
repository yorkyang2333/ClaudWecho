package com.example.claudwecho.ui.playlist

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
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    viewModel: PlaylistDetailViewModel = koinViewModel(),
    onNavigateToPlayer: (String, String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val songs by viewModel.songs.collectAsState()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
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
                items(songs) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = 4.dp)
                            .background(Color.DarkGray, RoundedCornerShape(8.dp))
                            .clickable {
                                val realUrl = "https://music.163.com/song/media/outer/url?id=${song.id}.mp3"
                                val encodedUrl = java.net.URLEncoder.encode(realUrl, "UTF-8")
                                val encodedTitle = java.net.URLEncoder.encode(song.name, "UTF-8")
                                onNavigateToPlayer(encodedUrl, encodedTitle)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.al.picUrl,
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(song.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                            Text(song.ar.firstOrNull()?.name ?: "Unknown Artist", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
