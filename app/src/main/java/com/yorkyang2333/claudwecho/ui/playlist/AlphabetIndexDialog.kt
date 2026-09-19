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
import com.yorkyang2333.claudwecho.ui.components.WearDialog
import com.yorkyang2333.claudwecho.ui.components.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import com.yorkyang2333.claudwecho.ui.components.WearListHeader

@Composable
fun AlphabetIndexDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onLetterSelected: (String) -> Unit
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
                    WearListHeader(title = "字母索引")
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
        }
    }
}
