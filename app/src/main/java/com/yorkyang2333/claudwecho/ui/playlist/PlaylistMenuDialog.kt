package com.yorkyang2333.claudwecho.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.dialog.Dialog
import com.yorkyang2333.claudwecho.ui.components.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.components.PinnedHeader

@Composable
fun PlaylistMenuDialog(
    showDialog: Boolean,
    isOwned: Boolean,
    onDismissRequest: () -> Unit,
    onPlayAll: () -> Unit,
    onMultiSelect: () -> Unit,
    onAlphabetIndex: () -> Unit,
    onSortBy: () -> Unit,
    currentSort: String,
    isAlphabetIndexEnabled: Boolean = true
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
                        onClick = onPlayAll,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(
                            text = "播放全部",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (isOwned) {
                    item {
                        Button(
                            onClick = onMultiSelect,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors()
                        ) {
                            Text(
                                text = "多选删除",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = onAlphabetIndex,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isAlphabetIndexEnabled,
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(
                            text = "字母索引",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                item {
                    Button(
                        onClick = onSortBy,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "排序方式",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentSort,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            PinnedHeader(title = "菜单")
        }
    }
}
