package com.yorkyang2333.claudwecho.ui.collection

import androidx.compose.foundation.background
import com.yorkyang2333.claudwecho.ui.components.hapticClickable
import com.yorkyang2333.claudwecho.ui.components.Button
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    val currentUserId by viewModel.currentUserId.collectAsState()

    var selectedTabIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val createdPlaylists = androidx.compose.runtime.remember(playlists, currentUserId) {
        val firstId = playlists.firstOrNull()?.id
        playlists.filter { it.isCreatedBy(currentUserId) && it.id != firstId && it.name != "我喜欢" }
    }
    val collectedPlaylists = androidx.compose.runtime.remember(playlists, currentUserId) {
        playlists.filter { !it.isCreatedBy(currentUserId) }
    }
    val displayList = if (selectedTabIndex == 0) createdPlaylists else collectedPlaylists

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            androidx.wear.compose.material3.CircularProgressIndicator()
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
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { selectedTabIndex = 0 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                colors = androidx.wear.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else Color(0xFF252320),
                                    contentColor = if (selectedTabIndex == 0) Color.Black else Color.White
                                )
                            ) {
                                Text(
                                    text = "创建",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { selectedTabIndex = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                colors = androidx.wear.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary else Color(0xFF252320),
                                    contentColor = if (selectedTabIndex == 1) Color.Black else Color.White
                                )
                            ) {
                                Text(
                                    text = "收藏",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (displayList.isEmpty()) {
                        item {
                            Text(
                                text = if (selectedTabIndex == 0) "暂无创建的歌单" else "暂无收藏的歌单",
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        items(displayList, key = { it.id }) { playlist ->
                            CollectionItemRow(
                                title = playlist.name,
                                subtitle = "${playlist.trackCount} 首",
                                imageUrl = playlist.coverImgUrl,
                                onClick = { onNavigateToPlaylistDetail(playlist.id) }
                            )
                        }
                    }
                }

                com.yorkyang2333.claudwecho.ui.components.PinnedHeader(
                    title = "歌单",
                    actionIcon = {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(0xFF2D2D2D))
                                .hapticClickable { viewModel.loadData(forceRefresh = true) },
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
fun MyCollectionAlbumsScreen(
    viewModel: MyCollectionViewModel,
    onNavigateToAlbumDetail: (Long) -> Unit
) {
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CollectionListBase(
        title = "专辑",
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

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
            androidx.wear.compose.material3.CircularProgressIndicator()
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
                                .hapticClickable { onRefresh() },
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
    Button(
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
