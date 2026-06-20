package com.yorkyang2333.claudwecho.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToPlaylistDetail: (Long) -> Unit,
    onNavigateToFeature: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                ScalingLazyColumn(
            scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                edgeScale = 0.3f,
                minTransitionArea = 0.4f
            ),
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                    // User Profile Section
                    item {
                        if (userProfile != null) {
                            Button(
                                onClick = { onNavigateToFeature("profile") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            label = {
                                Text(
                                    text = userProfile?.nickname ?: "User",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = {
                                userProfile?.avatarUrl?.let {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                    )
                                } ?: Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )
                    } else {
                        FeatureButton(
                            icon = Icons.Rounded.AccountCircle,
                            text = "登录网易云",
                            onClick = onNavigateToLogin
                        )
                    }
                }

                // Main Features
                item {
                    FeatureButton(
                        icon = Icons.Rounded.Favorite,
                        text = "我喜欢",
                        onClick = { onNavigateToFeature("liked") }
                    )
                }
                item {
                    FeatureButton(
                        icon = Icons.Rounded.Search,
                        text = "搜索",
                        onClick = { onNavigateToFeature("search") }
                    )
                }
                
                item {
                    FeatureButton(
                        icon = Icons.Rounded.Radio,
                        text = "私人 FM",
                        onClick = { onNavigateToFeature("personal_fm") }
                    )
                }
                
                item {
                    FeatureButton(
                        icon = Icons.Rounded.Today,
                        text = "每日推荐",
                        onClick = { onNavigateToFeature("daily_recommendation") }
                    )
                }

                item {
                    FeatureButton(
                        icon = Icons.Rounded.Star,
                        text = "我的收藏",
                        onClick = { onNavigateToFeature("my_collection") }
                    )
                }

                item {
                    FeatureButton(
                        icon = Icons.Rounded.History,
                        text = "最近播放",
                        onClick = { onNavigateToFeature("recently_played") }
                    )
                }

                item {
                    FeatureButton(
                        icon = Icons.Rounded.Settings,
                        text = "设置",
                        onClick = { onNavigateToFeature("settings") }
                    )
                }
            }
            com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "ClaudWecho")
        }
        }
    }
}

@Composable
fun FeatureButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.filledTonalButtonColors(),
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}
