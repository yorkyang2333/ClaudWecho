package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.yorkyang2333.claudwecho.data.LyricWord
import com.yorkyang2333.claudwecho.ui.components.RotaryScalingLazyColumn

@Composable
fun LyricsScreen(viewModel: PlayerViewModel, isActivePage: Boolean = true) {
    val lyrics by viewModel.lyrics.collectAsState()
    val currentLyricIndex by viewModel.currentLyricIndex.collectAsState()
    val currentTitle by viewModel.currentTrackTitle.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val verbatimLyricsEnabled by viewModel.verbatimLyricsEnabled.collectAsState()
    val listState = rememberScalingLazyListState()

    // 每次切歌 (currentTitle 变化) 或 启动/歌词载入 (lyrics 变化) 时，自动回顶
    LaunchedEffect(currentTitle, lyrics) {
        if (lyrics.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(currentLyricIndex) {
        if (currentLyricIndex >= 0 && currentLyricIndex < lyrics.size) {
            // Scroll to current lyric
            listState.animateScrollToItem(currentLyricIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
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
            RotaryScalingLazyColumn(
                autoCentering = AutoCenteringParams(itemIndex = 0),
                state = listState,
                isActivePage = isActivePage,
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isCurrent = index == currentLyricIndex

                    val scale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.08f else 0.9f,
                        animationSpec = tween(durationMillis = 300),
                        label = "scale"
                    )

                    val alpha by animateFloatAsState(
                        targetValue = if (isCurrent) 1.0f else 0.4f,
                        animationSpec = tween(durationMillis = 300),
                        label = "alpha"
                    )

                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = Color.White.copy(alpha = 0.5f)

                    val textColor by animateColorAsState(
                        targetValue = if (isCurrent) activeColor else Color.White,
                        animationSpec = tween(durationMillis = 300),
                        label = "color"
                    )

                    val tTextColor by animateColorAsState(
                        targetValue = if (isCurrent) activeColor.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f),
                        animationSpec = tween(durationMillis = 300),
                        label = "tcolor"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                    ) {
                        if (isCurrent && line.isVerbatim && verbatimLyricsEnabled) {
                            VerbatimLyricRow(
                                words = line.words,
                                currentPosition = currentPosition,
                                activeColor = activeColor,
                                inactiveColor = inactiveColor
                            )
                        } else {
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.8f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 8f
                                    )
                                ),
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (line.tText != null) {
                            Text(
                                text = line.tText!!,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.8f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 6f
                                    )
                                ),
                                color = tTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VerbatimLyricRow(
    words: List<LyricWord>,
    currentPosition: Long,
    activeColor: Color,
    inactiveColor: Color
) {
    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        words.forEach { word ->
            val fraction = if (currentPosition <= word.startTimeMs) {
                0f
            } else if (currentPosition >= word.endTimeMs) {
                1f
            } else {
                ((currentPosition - word.startTimeMs).toFloat() / word.durationMs.coerceAtLeast(10L).toFloat()).coerceIn(0f, 1f)
            }

            val brush = if (fraction <= 0f) {
                SolidColor(inactiveColor)
            } else if (fraction >= 1f) {
                SolidColor(activeColor)
            } else {
                val transition = 0.05f
                val p1 = (fraction - transition).coerceAtLeast(0f)
                val p2 = (fraction + transition).coerceAtMost(1f)
                Brush.horizontalGradient(
                    0f to activeColor,
                    p1 to activeColor,
                    p2 to inactiveColor,
                    1f to inactiveColor
                )
            }

            Text(
                text = word.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = Offset(2f, 2f),
                        blurRadius = 8f
                    ),
                    brush = brush
                )
            )
        }
    }
}
