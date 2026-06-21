package com.yorkyang2333.claudwecho.ui.collection

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
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import com.yorkyang2333.claudwecho.data.api.Album
import com.yorkyang2333.claudwecho.data.api.DjRadio
import com.yorkyang2333.claudwecho.data.api.Playlist
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh

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
    viewModel: MyCollectionViewModel,
    onNavigateToAlbumDetail: (Long) -> Unit
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
                title = album.name ?: "未知专辑",
                subtitle = "专辑",
                imageUrl = album.picUrl ?: "",
                onClick = { onNavigateToAlbumDetail(album.id) }
            )
        }
    )
}

@Composable
fun MyCollectionBlogsScreen(
    viewModel: MyCollectionViewModel,
    onNavigateToDjRadioDetail: (Long) -> Unit
) {
    val djRadios by viewModel.djRadios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    CollectionListBase(
        title = "播客",
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
                onClick = { onNavigateToDjRadioDetail(dj.id) }
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
            androidx.wear.compose.material.CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else if (items.isEmpty()) {
            Text(emptyMessage, color = Color.Gray)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                RotaryScalingLazyColumn(
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                    items(items, key = { keySelector(it) }) { item ->
                        itemContent(item)
                    }
                }
                
                com.yorkyang2333.claudwecho.ui.components.PinnedHeader(
                    title = title,
                    actionIcon = {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFF2D2D2D))
                                .clickable { onRefresh() },
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

@Composable
fun CollectionItemRow(
    title: String,
    subtitle: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    androidx.wear.compose.material3.Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.wear.compose.material3.ButtonDefaults.filledTonalButtonColors(),
        label = {
            androidx.wear.compose.material3.Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            androidx.wear.compose.material3.Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    )
}
