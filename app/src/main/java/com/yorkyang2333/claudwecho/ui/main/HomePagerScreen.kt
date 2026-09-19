package com.yorkyang2333.claudwecho.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.yorkyang2333.claudwecho.ui.player.LyricsScreen
import com.yorkyang2333.claudwecho.ui.player.PlayerScreen
import com.yorkyang2333.claudwecho.ui.player.PlayerViewModel

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.transformations

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagerScreen(
    playerViewModel: PlayerViewModel,
    initialPage: Int = 0,
    onNavigateToMenu: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val currentArtworkUri by playerViewModel.currentArtworkUri.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "playerBackgroundAnim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bgRotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 2.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Fluid Background using Album Art
        if (currentArtworkUri != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val isApi31AndAbove = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
            val imageRequest = ImageRequest.Builder(context)
                .data(currentArtworkUri)
                .apply {
                    if (!isApi31AndAbove) {
                        transformations(com.yorkyang2333.claudwecho.ui.utils.BlurTransformation(context, 25f))
                        size(100) // downscale to intensify blur effect
                    }
                }
                .build()

            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (isPlaying) {
                            rotationZ = rotation
                            scaleX = scale
                            scaleY = scale
                        }
                    }
                    .then(if (isApi31AndAbove) Modifier.blur(30.dp) else Modifier)
            )
            // Add a dark overlay so text is readable
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
        } else {
            // Default clean static gradient if no artwork (Zero GPU blur overhead during startup)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(
                                Color(0xFF2A2E35),
                                Color(0xFF1E2124),
                                Color(0xFF121416)
                            )
                        )
                    )
            )
        }

        HorizontalPager(
            state = pagerState
        ) { page ->
            when (page) {
                0 -> PlayerScreen(
                    viewModel = playerViewModel,
                    isActivePage = pagerState.currentPage == 0,
                    onMenuClick = onNavigateToMenu,
                    onSettingsClick = onSettingsClick
                )
                1 -> LyricsScreen(
                    viewModel = playerViewModel,
                    isActivePage = pagerState.currentPage == 1
                )
            }
        }
    }
}
