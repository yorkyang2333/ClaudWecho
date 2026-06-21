package com.yorkyang2333.claudwecho.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.ui.components.PinnedHeader

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
            
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                    edgeScale = 0.3f, 
                    minTransitionArea = 0.4f
                ),
                contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                autoCentering = null
            ) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
                item {
                    PinnedHeader(title = "排序方式")
                }
                
                val options = listOf(
                    Triple(SortMode.DEFAULT, SortOrder.ASC, "默认时间"),
                    Triple(SortMode.TITLE, SortOrder.ASC, "标题 (A-Z)"),
                    Triple(SortMode.TITLE, SortOrder.DESC, "标题 (Z-A)"),
                    Triple(SortMode.ALBUM, SortOrder.ASC, "专辑 (A-Z)"),
                    Triple(SortMode.ALBUM, SortOrder.DESC, "专辑 (Z-A)"),
                    Triple(SortMode.ARTIST, SortOrder.ASC, "歌手 (A-Z)"),
                    Triple(SortMode.ARTIST, SortOrder.DESC, "歌手 (Z-A)")
                )
                
                items(options.size) { index ->
                    val option = options[index]
                    val isSelected = currentSortMode == option.first && currentSortOrder == option.second
                    
                    Button(
                        onClick = {
                            onSortSelected(option.first, option.second)
                            onDismissRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isSelected) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) else ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Text(
                            text = option.third,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
