package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
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
    // Read the playback clock only during drawing, not while composing the list.
    val currentPosition = viewModel.currentPosition.collectAsState()
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
            // 平滑滚动至当前歌词行
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
            // 保留 itemIndex = 0 确保首句居中效果；切句时由 animateScrollToItem 驱动平滑滚动
            RotaryScalingLazyColumn(
                autoCentering = AutoCenteringParams(itemIndex = 0),
                state = listState,
                isActivePage = isActivePage,
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(
                    items = lyrics,
                    key = { index, line -> "${line.timeMs}_${index}" }
                ) { index, line ->
                    val isCurrent = index == currentLyricIndex

                    val scale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.05f else 0.95f,
                        animationSpec = tween(durationMillis = 300),
                        label = "scale"
                    )

                    val alpha by animateFloatAsState(
                        targetValue = if (isCurrent) 1.0f else 0.4f,
                        animationSpec = tween(durationMillis = 300),
                        label = "alpha"
                    )

                    val primaryColor = MaterialTheme.colorScheme.primary

                    // 当前句高亮为主色，非当前句（包括已唱完的上一句与未唱的下一句）平滑同步褪色为白色
                    val activeColor by animateColorAsState(
                        targetValue = if (isCurrent) primaryColor else Color.White,
                        animationSpec = tween(durationMillis = 300),
                        label = "activeColor"
                    )

                    val inactiveColor by animateColorAsState(
                        targetValue = if (isCurrent) Color.White.copy(alpha = 0.5f) else Color.White,
                        animationSpec = tween(durationMillis = 300),
                        label = "inactiveColor"
                    )

                    // 翻译颜色与原歌词状态完全同步，切句后上一句翻译同步褪色
                    val tTextColor by animateColorAsState(
                        targetValue = if (isCurrent) primaryColor.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.7f),
                        animationSpec = tween(durationMillis = 300),
                        label = "tcolor"
                    )

                    val mainTextStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(2f, 2f),
                            blurRadius = 8f
                        )
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
                        if (line.isVerbatim && verbatimLyricsEnabled) {
                            // 保持组件结构恒定，通过颜色与 Path 自然过渡，零闪烁
                            SweepingVerbatimLyricText(
                                text = line.text,
                                words = line.words,
                                currentPosition = {
                                    when {
                                        isCurrent -> currentPosition.value
                                        index < currentLyricIndex -> Long.MAX_VALUE
                                        else -> Long.MIN_VALUE
                                    }
                                },
                                activeColor = activeColor,
                                inactiveColor = inactiveColor,
                                baseTextStyle = mainTextStyle
                            )
                        } else {
                            Text(
                                text = line.text,
                                style = mainTextStyle,
                                color = activeColor,
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

/**
 * 逐字平滑扫光文本组件。
 * 保持底层与顶层组件结构完全稳定，通过动态几何 Path 裁切显示已唱区域。
 */
@Composable
private fun SweepingVerbatimLyricText(
    text: String,
    words: List<LyricWord>,
    currentPosition: () -> Long,
    activeColor: Color,
    inactiveColor: Color,
    baseTextStyle: androidx.compose.ui.text.TextStyle
) {
    var layoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    val clipPath = remember(text) { Path() }
    val wordBounds = remember(words, layoutResult) {
        val layout = layoutResult
        if (layout == null) emptyList() else buildVerbatimWordBounds(
            words = words,
            textLength = layout.layoutInput.text.length,
            boundingBox = layout::getBoundingBox
        )
    }

    Box(contentAlignment = Alignment.Center) {
        // 底层：未扫过状态的完整文本（自带阴影，保证文字可读性）
        Text(
            text = text,
            style = baseTextStyle,
            color = inactiveColor,
            textAlign = TextAlign.Center,
            onTextLayout = { layoutResult = it }
        )

        // 顶层：高亮文本，根据逐字播放进度动态裁切出已唱区域
        Text(
            text = text,
            style = baseTextStyle.copy(shadow = null),
            color = activeColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.drawWithContent {
                val position = currentPosition()
                clipPath.reset()

                for (wordIndex in wordBounds.indices) {
                    val word = words[wordIndex]
                    val bounds = wordBounds[wordIndex]
                    val fraction = when {
                        position >= word.endTimeMs -> 1f
                        position <= word.startTimeMs -> 0f
                        else -> ((position - word.startTimeMs).toFloat() /
                            word.durationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
                    }
                    val sweptBounds = fraction * bounds.size
                    for (index in bounds.indices) {
                        val rect = bounds[index]
                        val sweptFraction = (sweptBounds - index).coerceIn(0f, 1f)
                        if (sweptFraction <= 0f) break
                        clipPath.addRect(
                            if (sweptFraction == 1f) rect else Rect(
                                left = rect.left,
                                top = rect.top,
                                right = rect.left + rect.width * sweptFraction,
                                bottom = rect.bottom
                            )
                        )
                    }
                }

                clipPath(clipPath) {
                    this@drawWithContent.drawContent()
                }
            }
        )
    }
}
