package com.yorkyang2333.claudwecho.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
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

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
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

    val isPodcast by viewModel.isCurrentSongPodcast.collectAsState()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clipToBounds()
            .onRotaryScrollEvent { event ->
                val deltaMs = (event.verticalScrollPixels * 100).toLong()
                val newPos = (currentPosition + deltaMs).coerceIn(0L, duration)
                viewModel.seekTo(newPos)
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {

        // Circular Progress at edge
        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
        androidx.wear.compose.material3.CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            colors = androidx.wear.compose.material3.ProgressIndicatorDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(36.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = currentTitle ?: "暂无播放内容",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                if (currentTitle != null) {
                    Text(
                        text = currentArtist ?: "未知歌手",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }

                Spacer(modifier = Modifier.weight(1f))

                // Playback Controls
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFmMode) {
                        IconButton(
                            onClick = {
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
                        IconButton(
                            onClick = { viewModel.skipToPrevious() },
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

                    IconButton(
                        onClick = {
                            viewModel.playOrPause()
                        },
                        modifier = Modifier.size(64.dp),
                        enabled = currentTitle != null,
                        colors = androidx.wear.compose.material3.IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (currentTitle == null) Color.Gray else Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.skipToNext() },
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

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Menu Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPodcast) {
                        Box(modifier = Modifier.size(44.dp).offset(y = (-8).dp))
                    } else {
                        IconButton(
                            onClick = { viewModel.toggleLikeCurrentSong() },
                            modifier = Modifier.size(44.dp).offset(y = (-8).dp),
                            enabled = currentTitle != null,
                            colors = androidx.wear.compose.material3.IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (currentTitle == null) Color.Gray else if (isLiked) Color.Red else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(44.dp).offset(y = 4.dp),
                        colors = androidx.wear.compose.material3.IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = "Home",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(44.dp).offset(y = (-8).dp),
                        colors = androidx.wear.compose.material3.IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
    }
}
