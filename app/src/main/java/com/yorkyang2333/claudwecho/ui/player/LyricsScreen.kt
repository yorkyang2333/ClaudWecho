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
            // 平滑滚动至当前歌词行，避免跳变
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
            // 采用固定居中参数，避免切句时频繁重建 Spacer 导致画面闪烁抽动
            RotaryScalingLazyColumn(
                autoCentering = AutoCenteringParams(itemIndex = 1),
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
                        animationSpec = tween(durationMillis = 350),
                        label = "scale"
                    )

                    val alpha by animateFloatAsState(
                        targetValue = if (isCurrent) 1.0f else 0.4f,
                        animationSpec = tween(durationMillis = 350),
                        label = "alpha"
                    )

                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = Color.White.copy(alpha = 0.6f)

                    val textColor by animateColorAsState(
                        targetValue = if (isCurrent) activeColor else Color.White,
                        animationSpec = tween(durationMillis = 350),
                        label = "color"
                    )

                    val tTextColor by animateColorAsState(
                        targetValue = if (isCurrent) activeColor.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f),
                        animationSpec = tween(durationMillis = 350),
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
                            // 保持组件结构恒定，切句时不发生组件销毁/重建与首帧闪烁
                            SweepingVerbatimLyricText(
                                text = line.text,
                                words = line.words,
                                currentPosition = currentPosition,
                                activeColor = activeColor,
                                inactiveColor = inactiveColor,
                                baseTextStyle = mainTextStyle
                            )
                        } else {
                            Text(
                                text = line.text,
                                style = mainTextStyle,
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

/**
 * 逐字平滑扫光文本组件。
 * 保持底层与顶层组件结构完全稳定，通过动态几何 Path 裁切显示已唱区域。
 */
@Composable
private fun SweepingVerbatimLyricText(
    text: String,
    words: List<LyricWord>,
    currentPosition: Long,
    activeColor: Color,
    inactiveColor: Color,
    baseTextStyle: androidx.compose.ui.text.TextStyle
) {
    var layoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    val clipPath = remember(text) { Path() }

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
                val layout = layoutResult ?: return@drawWithContent
                clipPath.reset()

                var charOffset = 0
                for (word in words) {
                    val wordLen = word.text.length
                    val wordStart = word.startTimeMs
                    val wordDur = word.durationMs.coerceAtLeast(1L)
                    val wordEnd = word.endTimeMs

                    if (currentPosition >= wordEnd) {
                        // 词已唱完：完整裁切整词区域
                        val cStart = charOffset.coerceIn(0, layout.layoutInput.text.length)
                        val cEnd = (charOffset + wordLen - 1).coerceIn(0, layout.layoutInput.text.length - 1)
                        if (cStart <= cEnd) {
                            val firstBox = layout.getBoundingBox(cStart)
                            val lastBox = layout.getBoundingBox(cEnd)
                            if (firstBox.top == lastBox.top) {
                                clipPath.addRect(
                                    Rect(
                                        left = firstBox.left,
                                        top = firstBox.top,
                                        right = lastBox.right,
                                        bottom = firstBox.bottom
                                    )
                                )
                            } else {
                                for (i in 0 until wordLen) {
                                    val ci = charOffset + i
                                    if (ci < layout.layoutInput.text.length) {
                                        clipPath.addRect(layout.getBoundingBox(ci))
                                    }
                                }
                            }
                        }
                    } else if (currentPosition > wordStart) {
                        // 词正在唱：按时间比例从左到右平滑扫光
                        val fraction = ((currentPosition - wordStart).toFloat() / wordDur.toFloat()).coerceIn(0f, 1f)
                        val cStart = charOffset.coerceIn(0, layout.layoutInput.text.length)
                        val cEnd = (charOffset + wordLen - 1).coerceIn(0, layout.layoutInput.text.length - 1)
                        if (cStart <= cEnd) {
                            val firstBox = layout.getBoundingBox(cStart)
                            val lastBox = layout.getBoundingBox(cEnd)
                            if (firstBox.top == lastBox.top) {
                                val wordLeft = firstBox.left
                                val wordRight = lastBox.right
                                val sweepRight = wordLeft + (wordRight - wordLeft) * fraction
                                clipPath.addRect(
                                    Rect(
                                        left = wordLeft,
                                        top = firstBox.top,
                                        right = sweepRight,
                                        bottom = firstBox.bottom
                                    )
                                )
                            } else {
                                val totalCharsFraction = fraction * wordLen
                                val completedChars = totalCharsFraction.toInt()
                                val partialCharFraction = totalCharsFraction - completedChars

                                for (i in 0 until completedChars) {
                                    val ci = charOffset + i
                                    if (ci < layout.layoutInput.text.length) {
                                        clipPath.addRect(layout.getBoundingBox(ci))
                                    }
                                }
                                if (completedChars < wordLen) {
                                    val ci = charOffset + completedChars
                                    if (ci < layout.layoutInput.text.length) {
                                        val cBox = layout.getBoundingBox(ci)
                                        clipPath.addRect(
                                            Rect(
                                                left = cBox.left,
                                                top = cBox.top,
                                                right = cBox.left + cBox.width * partialCharFraction,
                                                bottom = cBox.bottom
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    charOffset += wordLen
                }

                clipPath(clipPath) {
                    this@drawWithContent.drawContent()
                }
            }
        )
    }
}
