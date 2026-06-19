package com.example.claudwecho.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun LyricsScreen(viewModel: PlayerViewModel) {
    val lyrics by viewModel.lyrics.collectAsState()
    val currentLyricIndex by viewModel.currentLyricIndex.collectAsState()
    val currentTitle by viewModel.currentTrackTitle.collectAsState()
    val listState = rememberScalingLazyListState()

    LaunchedEffect(currentLyricIndex) {
        if (currentLyricIndex >= 0 && currentLyricIndex < lyrics.size) {
            // Scroll to current lyric
            listState.animateScrollToItem(currentLyricIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (currentTitle == null) {
            Text(
                text = "暂无播放内容",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else if (lyrics.isEmpty()) {
            Text(
                text = "暂无歌词",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            ScalingLazyColumn(
        autoCentering = androidx.wear.compose.foundation.lazy.AutoCenteringParams(itemIndex = 1),
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isCurrent = index == currentLyricIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = line.text,
                            style = if (isCurrent) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        if (line.tText != null) {
                            Text(
                                text = line.tText!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
