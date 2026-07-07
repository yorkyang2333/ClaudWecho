package com.yorkyang2333.claudwecho.ui.playlist

import androidx.compose.foundation.background
import com.yorkyang2333.claudwecho.ui.components.hapticClickable
import com.yorkyang2333.claudwecho.ui.components.Button
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Close
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch

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
    val isOwned by viewModel.isOwnedPlaylist.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(playlistId, type) {
        when (type) {
            "playlist" -> viewModel.loadPlaylist(playlistId)
            "album" -> viewModel.loadAlbum(playlistId)
            "djradio" -> viewModel.loadDjRadio(playlistId)
            "liked" -> viewModel.loadLiked()
            else -> viewModel.loadPlaylist(playlistId)
        }
    }

    val searchLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val results = android.app.RemoteInput.getResultsFromIntent(data)
                val query = results?.getCharSequence("search_query")?.toString()
                if (query != null) {
                    viewModel.setSearchQuery(query)
                }
            }
        }
    }

    val showMenu = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val showSort = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val showAlphabet = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val showSearchDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var tempSearchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    
    val isMultiSelectMode = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val selectedSongs = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateListOf<Long>() }
    
    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
                    state = listState,
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                    if (isMultiSelectMode.value) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { 
                                        isMultiSelectMode.value = false 
                                        selectedSongs.clear()
                                    },
                                    colors = androidx.wear.compose.material3.ButtonDefaults.filledTonalButtonColors(),
                                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                                ) {
                                    Text("取消", style = MaterialTheme.typography.bodyMedium)
                                }
                                Button(
                                    onClick = { 
                                        if (selectedSongs.isNotEmpty()) {
                                            viewModel.removeSongs(selectedSongs.toList(), type == "liked")
                                            selectedSongs.clear()
                                            isMultiSelectMode.value = false
                                        }
                                    },
                                    colors = androidx.wear.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                                ) {
                                    Text("删除(${selectedSongs.size})", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    } else {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Search
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E1E1E))
                                        .hapticClickable {
                                            if (searchQuery.isNotEmpty()) {
                                                viewModel.setSearchQuery("")
                                            } else {
                                                val intent = androidx.wear.input.RemoteInputIntentHelper.createActionRemoteInputIntent()
                                                val remoteInputs = listOf(
                                                    android.app.RemoteInput.Builder("search_query")
                                                        .setLabel("搜索播放列表")
                                                        .build()
                                                )
                                                androidx.wear.input.RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
                                                try {
                                                    searchLauncher.launch(intent)
                                                } catch (e: Exception) {
                                                    showSearchDialog.value = true
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.wear.compose.material3.Icon(
                                        imageVector = if (searchQuery.isNotEmpty()) androidx.compose.material.icons.Icons.Rounded.Close else Icons.Rounded.Search,
                                        contentDescription = if (searchQuery.isNotEmpty()) "Clear Search" else "Search",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                // Count or Query
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E1E1E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) searchQuery else "${songs.size}首",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                
                                // Menu
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1E1E1E))
                                        .hapticClickable { showMenu.value = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.wear.compose.material3.Icon(
                                        Icons.Rounded.Menu,
                                        contentDescription = "Menu",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    items(songs.size, key = { songs[it].id }) { index ->
                        val song = songs[index]
                        val isSelected = selectedSongs.contains(song.id)
                        
                        com.yorkyang2333.claudwecho.ui.components.SharedSongItem(
                            song = song,
                            isMultiSelectMode = isMultiSelectMode.value,
                            isSelected = isSelected,
                            onClick = { 
                                if (isMultiSelectMode.value) {
                                    if (isSelected) selectedSongs.remove(song.id)
                                    else selectedSongs.add(song.id)
                                } else {
                                    onNavigateToPlayer(songs, index)
                                }
                            }
                        )
                    }
                }

                com.yorkyang2333.claudwecho.ui.components.PinnedHeader(
                    title = title ?: when (type) {
                        "liked" -> "我喜欢"
                        "playlist" -> "歌单"
                        "album" -> "专辑"
                        "djradio" -> "播客"
                        else -> "音乐列表"
                    },
                    actionIcon = if (type == "liked") {
                        {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color(0xFF2D2D2D))
                                    .hapticClickable { viewModel.loadLiked(forceRefresh = true) },
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
            
            val currentSortText = when (sortMode) {
                SortMode.DEFAULT -> "添加时间"
                SortMode.TITLE -> if (sortOrder == SortOrder.ASC) "标题 (A-Z)" else "标题 (Z-A)"
                SortMode.ALBUM -> if (sortOrder == SortOrder.ASC) "专辑 (A-Z)" else "专辑 (Z-A)"
                SortMode.ARTIST -> if (sortOrder == SortOrder.ASC) "歌手 (A-Z)" else "歌手 (Z-A)"
            }

            PlaylistMenuDialog(
                showDialog = showMenu.value,
                isOwned = isOwned,
                onDismissRequest = { showMenu.value = false },
                onPlayAll = {
                    if (songs.isNotEmpty()) {
                        onNavigateToPlayer(songs, 0)
                        showMenu.value = false
                    }
                },
                onMultiSelect = {
                    isMultiSelectMode.value = true
                    showMenu.value = false
                },
                onAlphabetIndex = {
                    showAlphabet.value = true
                },
                onSortBy = {
                    showSort.value = true
                },
                currentSort = currentSortText,
                isAlphabetIndexEnabled = sortMode != SortMode.DEFAULT
            )
            
            PlaylistSortDialog(
                showDialog = showSort.value,
                currentSortMode = sortMode,
                currentSortOrder = sortOrder,
                onDismissRequest = {
                    showSort.value = false
                },
                onSortSelected = { mode, order ->
                    viewModel.setSort(mode, order)
                }
            )
            
            AlphabetIndexDialog(
                showDialog = showAlphabet.value,
                onDismissRequest = {
                    showAlphabet.value = false
                },
                onLetterSelected = { letter ->
                    val index = viewModel.getFirstItemIndexByLetter(letter)
                    if (index >= 0) {
                        coroutineScope.launch {
                            // index + 2 to account for spacer and header items
                            listState.scrollToItem(index + 2)
                        }
                    }
                    showAlphabet.value = false
                    showMenu.value = false
                }
            )

            if (showSearchDialog.value) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showSearchDialog.value = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "搜索播放列表",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = tempSearchQuery,
                            onValueChange = { tempSearchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = androidx.compose.ui.graphics.Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(androidx.compose.ui.graphics.Color.DarkGray)
                                .padding(8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = { showSearchDialog.value = false },
                                colors = androidx.wear.compose.material3.ButtonDefaults.filledTonalButtonColors(),
                                modifier = Modifier.size(48.dp)
                            ) {
                                androidx.wear.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.Close, null)
                            }
                            Button(
                                onClick = {
                                    viewModel.setSearchQuery(tempSearchQuery)
                                    showSearchDialog.value = false
                                },
                                colors = androidx.wear.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(48.dp)
                            ) {
                                androidx.wear.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.Search, null)
                            }
                        }
                    }
                }
            }
        }
    }
}
