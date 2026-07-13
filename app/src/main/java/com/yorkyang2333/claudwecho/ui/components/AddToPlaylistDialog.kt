package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.data.api.Playlist

@Composable
fun AddToPlaylistDialog(
    showDialog: Boolean,
    playlists: List<Playlist>,
    onDismissRequest: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit
) {
    Dialog(
        showDialog = showDialog,
        onDismissRequest = onDismissRequest
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val listState = rememberScalingLazyListState()

            RotaryScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                autoCentering = null
            ) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
                if (playlists.isEmpty()) {
                    item {
                        Text(
                            text = "暂无创建的歌单",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                } else {
                    items(playlists, key = { it.id }) { playlist ->
                        Button(
                            onClick = { onPlaylistSelected(playlist) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            PinnedHeader(title = "添加到歌单")
        }
    }
}
