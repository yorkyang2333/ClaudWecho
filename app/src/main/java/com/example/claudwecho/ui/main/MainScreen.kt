package com.example.claudwecho.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToPlayer: (String, String) -> Unit,
    onNavigateToPlaylistDetail: (Long) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val dailySongs by viewModel.dailySongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

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
            Text("Loading...", color = MaterialTheme.colorScheme.primary)
        } else {
            ScalingLazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (userProfile != null) {
                            userProfile?.avatarUrl?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                            }
                            Text(
                                text = userProfile?.nickname ?: "User",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                            )
                        } else {
                            androidx.wear.compose.material3.Button(
                                onClick = onNavigateToLogin,
                                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                            ) {
                                Text("Login for more")
                            }
                        }
                    }
                }

                if (dailySongs.isNotEmpty()) {
                    item {
                        Text(
                            if (userProfile != null) "Daily Recommendations" else "Hot Songs",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(dailySongs.take(5)) { song ->
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

                if (playlists.isNotEmpty()) {
                    item {
                        Text(
                            "My Playlists",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(vertical = 4.dp)
                                .background(Color.DarkGray, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .clickable { onNavigateToPlaylistDetail(playlist.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = playlist.coverImgUrl,
                                contentDescription = "Playlist Cover",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(playlist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                Text("${playlist.trackCount} tracks", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
                
                // Add a refresh button at the bottom
                item {
                    Button(
                        onClick = { viewModel.loadData(forceRefresh = true) },
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    ) {
                        Text("Refresh")
                    }
                }
            }
        }
    }
}
