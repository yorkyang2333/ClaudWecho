package com.yorkyang2333.claudwecho.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.dialog.Dialog
import com.yorkyang2333.claudwecho.ui.components.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.components.PinnedHeader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp

@Composable
fun PlaylistSortDialog(
    showDialog: Boolean,
    currentSortMode: SortMode,
    currentSortOrder: SortOrder,
    onDismissRequest: () -> Unit,
    onSortSelected: (SortMode, SortOrder) -> Unit
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
                            val newOrder = if (currentSortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
                            onSortSelected(currentSortMode, newOrder)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        icon = {
                            androidx.wear.compose.material3.Icon(
                                imageVector = if (currentSortOrder == SortOrder.ASC) androidx.compose.material.icons.Icons.Rounded.KeyboardArrowUp else androidx.compose.material.icons.Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Sort Order",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    ) {
                        Text(
                            text = if (currentSortOrder == SortOrder.ASC) "升序排序" else "降序排序",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    }
                }
                
                val options = listOf(
                    Pair(SortMode.DEFAULT, "添加时间"),
                    Pair(SortMode.TITLE, "按标题"),
                    Pair(SortMode.ALBUM, "按专辑"),
                    Pair(SortMode.ARTIST, "按歌手")
                )
                
                items(options.size) { index ->
                    val option = options[index]
                    val isSelected = currentSortMode == option.first
                    
                    Button(
                        onClick = {
                            onSortSelected(option.first, currentSortOrder)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isSelected) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) else ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(
                            text = option.second,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    }
                }
            }
            
            PinnedHeader(title = "排序方式")
        }
    }
}
