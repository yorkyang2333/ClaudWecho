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
import androidx.compose.ui.text.style.TextOverflow
import com.yorkyang2333.claudwecho.ui.components.PinnedHeader

@Composable
fun AlphabetIndexDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onLetterSelected: (String) -> Unit
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
                
                val letters = listOf("#") + ('A'..'Z').map { it.toString() }
                val rows = letters.chunked(3)
                
                items(rows.size) { index ->
                    val rowLetters = rows[index]
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowLetters.forEach { letter ->
                            Button(
                                onClick = {
                                    onLetterSelected(letter)
                                },
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                colors = ButtonDefaults.filledTonalButtonColors()
                            ) {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        repeat(3 - rowLetters.size) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        }
                    }
                }
            }
            
            PinnedHeader(title = "字母索引")
        }
    }
}
