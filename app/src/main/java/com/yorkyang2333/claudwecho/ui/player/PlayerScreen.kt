package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import com.yorkyang2333.claudwecho.ui.components.performClickHaptic
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import com.yorkyang2333.claudwecho.ui.components.hapticClickable
import com.yorkyang2333.claudwecho.ui.components.performClickHaptic
import com.yorkyang2333.claudwecho.ui.components.performRotaryHaptic
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    isActivePage: Boolean = true,
    onMenuClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTitle by viewModel.currentTrackTitle.collectAsState()
    val currentArtist by viewModel.currentArtistName.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isLiked by viewModel.isCurrentSongLiked.collectAsState()
    val isFmMode by viewModel.isPersonalFmMode.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE) }
    var screenShapeSetting by remember(prefs) { mutableStateOf(prefs.getString("screen_shape", "auto") ?: "auto") }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "screen_shape") {
                screenShapeSetting = prefs.getString("screen_shape", "auto") ?: "auto"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val config = LocalConfiguration.current
    val isRound = remember(screenShapeSetting, config) {
        when (screenShapeSetting) {
            "round" -> true
            "square" -> false
            else -> config.isScreenRound
        }
    }

    val isPodcast by viewModel.isCurrentSongPodcast.collectAsState()
    val isVip by viewModel.isCurrentSongVip.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val view = LocalView.current
    var accumulatedRotaryPx by remember { mutableStateOf(0f) }
    var lastRotaryHapticTime by remember { mutableStateOf(0L) }
    var accumulatedOppoScroll by remember { mutableStateOf(0f) }
    var lastOppoScrollTime by remember { mutableStateOf(0L) }
    var targetSeekPos by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(isActivePage) {
        if (isActivePage) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(targetSeekPos) {
        val target = targetSeekPos
        if (target != null) {
            delay(350)
            viewModel.seekTo(target)
            delay(600)
            targetSeekPos = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clipToBounds()
            .onRotaryScrollEvent { event ->
                if (duration <= 0L) return@onRotaryScrollEvent false
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastRotaryHapticTime > 250L) {
                    accumulatedRotaryPx = 0f
                }
                accumulatedRotaryPx += event.verticalScrollPixels
                if (Math.abs(accumulatedRotaryPx) >= 30f && currentTime - lastRotaryHapticTime >= 35L) {
                    view.performRotaryHaptic()
                    accumulatedRotaryPx = 0f
                    lastRotaryHapticTime = currentTime
                }
                val basePos = targetSeekPos ?: currentPosition
                val deltaMs = (-event.verticalScrollPixels * 25).toLong()
                val newPos = (basePos + deltaMs).coerceIn(0L, duration)
                targetSeekPos = newPos
                true
            }
            .pointerInteropFilter { event ->
                if (event.action == android.view.MotionEvent.ACTION_SCROLL) {
                    if (duration <= 0L) return@pointerInteropFilter false
                    val vScroll = event.getAxisValue(android.view.MotionEvent.AXIS_VSCROLL)
                    if (vScroll != 0f) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastOppoScrollTime > 300L) {
                            accumulatedOppoScroll = 0f
                        }
                        accumulatedOppoScroll += -vScroll
                        lastOppoScrollTime = currentTime

                        val threshold = 3.5f
                        if (Math.abs(accumulatedOppoScroll) >= threshold) {
                            val steps = (accumulatedOppoScroll / threshold).toInt()
                            val basePos = targetSeekPos ?: currentPosition
                            val deltaMs = steps * 1000L
                            val newPos = (basePos + deltaMs).coerceIn(0L, duration)
                            targetSeekPos = newPos
                            accumulatedOppoScroll -= steps * threshold

                            if (currentTime - lastRotaryHapticTime >= 35L) {
                                view.performRotaryHaptic()
                                lastRotaryHapticTime = currentTime
                            }
                        }
                        true
                    } else false
                } else false
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {

        // Circular Progress at edge
        val displayPosition = targetSeekPos ?: currentPosition
        val targetProgress = if (duration > 0) displayPosition.toFloat() / duration else 0f
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = if (targetSeekPos != null) 80 else 200, 
                easing = androidx.compose.animation.core.LinearEasing
            ),
            label = "progressAnim"
        )
        
        if (isRound) {
            androidx.wear.compose.material.CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 6.dp,
                indicatorColor = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
                    .padding(horizontal = if (isRound) 42.dp else 24.dp)
            ) {
                Text(
                    text = currentTitle ?: "暂无播放内容",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )
                if (currentTitle != null) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (targetSeekPos != null) {
                            Text(
                                text = "${formatTime(targetSeekPos!!)} / ${formatTime(duration)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )
                        } else {
                            Text(
                                text = currentArtist ?: "未知歌手",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )
                        }
                        if (isVip) {
                            Text(
                                text = "VIP",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            // Playback Controls
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFmMode) {
                    PlayerIconButton(
                        onClick = {
                            focusRequester.requestFocus()
                            viewModel.trashCurrentFmSong()
                            android.widget.Toast.makeText(context, "已添加到黑名单", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(56.dp),
                        enabled = currentTitle != null
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Trash",
                            tint = if (currentTitle == null) Color.Gray else Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    PlayerIconButton(
                        onClick = {
                            focusRequester.requestFocus()
                            viewModel.skipToPrevious()
                        },
                        modifier = Modifier.size(56.dp),
                        enabled = currentTitle != null
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = if (currentTitle == null) Color.Gray else Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                PlayerIconButton(
                    onClick = {
                        focusRequester.requestFocus()
                        viewModel.playOrPause()
                    },
                    modifier = Modifier.size(64.dp),
                    enabled = currentTitle != null,
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (currentTitle == null) Color.Gray else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                PlayerIconButton(
                    onClick = {
                        focusRequester.requestFocus()
                        viewModel.skipToNext()
                    },
                    modifier = Modifier.size(56.dp),
                    enabled = currentTitle != null
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = if (currentTitle == null) Color.Gray else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Bottom Menu Row
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isRound) {
                    var trackWidthPx by remember { mutableStateOf(1f) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(28.dp)
                            .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                            .pointerInput(duration) {
                                if (duration > 0L) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { offset ->
                                            val ratio = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                                            targetSeekPos = (ratio * duration).toLong()
                                        },
                                        onHorizontalDrag = { change, _ ->
                                            change.consume()
                                            val ratio = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                                            targetSeekPos = (ratio * duration).toLong()
                                        }
                                    )
                                }
                            }
                            .pointerInput(duration) {
                                if (duration > 0L) {
                                    detectTapGestures { offset ->
                                        val ratio = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                                        targetSeekPos = (ratio * duration).toLong()
                                        view.performClickHaptic()
                                    }
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(3.dp))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerIconButton(
                        onClick = {
                            focusRequester.requestFocus()
                            if (!isPodcast) viewModel.toggleLikeCurrentSong()
                        },
                        modifier = Modifier.size(44.dp).offset(y = (-8).dp),
                        enabled = currentTitle != null && !isPodcast
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (currentTitle == null || isPodcast) Color.Gray else if (isLiked) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    PlayerIconButton(
                        onClick = {
                            focusRequester.requestFocus()
                            onMenuClick()
                        },
                        modifier = Modifier.size(44.dp).offset(y = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Home",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    PlayerIconButton(
                        onClick = {
                            focusRequester.requestFocus()
                            onSettingsClick()
                        },
                        modifier = Modifier.size(44.dp).offset(y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    disabledContainerColor: Color = Color.Transparent,
    shape: Shape = CircleShape,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isVisualPressed by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        var pressStartTime = 0L
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    pressStartTime = System.currentTimeMillis()
                    isVisualPressed = true
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    val elapsed = System.currentTimeMillis() - pressStartTime
                    val minDuration = 150L
                    if (elapsed < minDuration) {
                        delay(minDuration - elapsed)
                    }
                    isVisualPressed = false
                }
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisualPressed && enabled) 0.88f else 1f,
        animationSpec = if (isVisualPressed && enabled) {
            tween(durationMillis = 100, easing = FastOutSlowInEasing)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "pressScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(if (enabled) containerColor else disabledContainerColor)
            .hapticClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
