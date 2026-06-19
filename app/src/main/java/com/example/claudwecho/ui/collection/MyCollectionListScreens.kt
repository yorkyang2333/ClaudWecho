package com.example.claudwecho.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.example.claudwecho.data.api.Album
import com.example.claudwecho.data.api.DjRadio
import com.example.claudwecho.data.api.Playlist
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh

@Composable
fun MyCollectionPlaylistsScreen(
    viewModel: MyCollectionViewModel,
    onNavigateToPlaylistDetail: (Long) -> Unit
) {
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    CollectionListBase(
        title = "我的歌单",
        items = playlists,
        isLoading = isLoading,
        emptyMessage = "暂无歌单",
        keySelector = { it.id },
        onRefresh = { viewModel.loadData(forceRefresh = true) },
        itemContent = { playlist ->
            CollectionItemRow(
                title = playlist.name,
                subtitle = "${playlist.trackCount} 首",
                imageUrl = playlist.coverImgUrl,
                onClick = { onNavigateToPlaylistDetail(playlist.id) }
            )
        }
    )
}

@Composable
fun MyCollectionAlbumsScreen(
    viewModel: MyCollectionViewModel
) {
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    CollectionListBase(
        title = "收藏的专辑",
        items = albums,
        isLoading = isLoading,
        emptyMessage = "暂无专辑",
        keySelector = { it.id },
        onRefresh = { viewModel.loadData(forceRefresh = true) },
        itemContent = { album ->
            CollectionItemRow(
                title = album.name,
                subtitle = "专辑",
                imageUrl = album.picUrl,
                onClick = { /* Not implemented yet */ }
            )
        }
    )
}

@Composable
fun MyCollectionBlogsScreen(
    viewModel: MyCollectionViewModel
) {
    val djRadios by viewModel.djRadios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    CollectionListBase(
        title = "订阅的播客",
        items = djRadios,
        isLoading = isLoading,
        emptyMessage = "暂无播客",
        keySelector = { it.id },
        onRefresh = { viewModel.loadData(forceRefresh = true) },
        itemContent = { dj ->
            CollectionItemRow(
                title = dj.name,
                subtitle = "播客",
                imageUrl = dj.picUrl,
                onClick = { /* Not implemented yet */ }
            )
        }
    )
}

@Composable
fun <T> CollectionListBase(
    title: String,
    items: List<T>,
    isLoading: Boolean,
    emptyMessage: String,
    keySelector: (T) -> Long,
    onRefresh: () -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Text("Loading...", color = MaterialTheme.colorScheme.primary)
        } else if (items.isEmpty()) {
            Text(emptyMessage, color = Color.Gray)
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
                            title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.wear.compose.material3.CompactButton(
                            onClick = onRefresh,
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
                items(items, key = { keySelector(it) }) { item ->
                    itemContent(item)
                }
            }
        }
    }
}

@Composable
fun CollectionItemRow(
    title: String,
    subtitle: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2D2D2D))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
