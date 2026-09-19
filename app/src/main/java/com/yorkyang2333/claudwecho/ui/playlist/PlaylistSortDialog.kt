package com.yorkyang2333.claudwecho.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn
import com.yorkyang2333.claudwecho.ui.components.rotaryContentPadding
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ButtonDefaults
import com.yorkyang2333.claudwecho.ui.components.WearDialog
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.components.Button
import com.yorkyang2333.claudwecho.ui.components.WearListHeader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material3.Icon

@Composable
fun PlaylistSortDialog(
    showDialog: Boolean,
    currentSortMode: SortMode,
    currentSortOrder: SortOrder,
    onDismissRequest: () -> Unit,
    onSortSelected: (SortMode, SortOrder) -> Unit
) {
    WearDialog(
        visible = showDialog,
        onDismissRequest = onDismissRequest
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val listState = rememberScalingLazyListState()
            
            RotaryScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = rotaryContentPadding(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                autoCentering = null
            ) {
                item {
                    WearListHeader(title = "排序方式")
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
                            Icon(
                                imageVector = if (currentSortOrder == SortOrder.ASC) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Sort Order",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = if (currentSortOrder == SortOrder.ASC) "升序排序" else "降序排序",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
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
                    val iconVector = when (option.first) {
                        SortMode.DEFAULT -> Icons.Rounded.Schedule
                        SortMode.TITLE -> Icons.Rounded.MusicNote
                        SortMode.ALBUM -> Icons.Rounded.Album
                        SortMode.ARTIST -> Icons.Rounded.Person
                    }
                    
                    Button(
                        onClick = {
                            onSortSelected(option.first, currentSortOrder)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isSelected) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) else ButtonDefaults.filledTonalButtonColors(),
                        icon = {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = option.second,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = option.second,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    }
}
