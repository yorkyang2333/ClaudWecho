package com.yorkyang2333.claudwecho.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.data.api.Song

@Composable
fun SongMenuDialog(
    showDialog: Boolean,
    song: Song?,
    onDismissRequest: () -> Unit,
    onPlayNext: () -> Unit,
    onSongInfo: (Long) -> Unit,
    canRemove: Boolean = false,
    onRemove: (() -> Unit)? = null,
    onAddToPlaylist: ((Song) -> Unit)? = null
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
                item {
                    Button(
                        onClick = {
                            onPlayNext()
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(
                            text = "下一首播放",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (canRemove && onRemove != null) {
                    item {
                        Button(
                            onClick = {
                                onRemove()
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(
                                text = "移除",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (onAddToPlaylist != null) {
                    item {
                        Button(
                            onClick = {
                                song?.let { onAddToPlaylist(it) }
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(
                                text = "添加到...",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            song?.let { onSongInfo(it.id) }
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(
                            text = "歌曲信息",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            PinnedHeader(title = song?.name ?: "歌曲菜单")
        }
    }
}
