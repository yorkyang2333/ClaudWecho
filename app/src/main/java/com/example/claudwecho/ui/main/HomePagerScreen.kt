package com.example.claudwecho.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.claudwecho.ui.player.LyricsScreen
import com.example.claudwecho.ui.player.PlayerScreen
import com.example.claudwecho.ui.player.PlayerViewModel

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
import coil.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagerScreen(
    playerViewModel: PlayerViewModel,
    initialPage: Int = 0,
    onNavigateToMenu: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val currentArtworkUri by playerViewModel.currentArtworkUri.collectAsState()
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 2.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Fluid Background using Album Art
        if (currentArtworkUri != null) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val isApi31AndAbove = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
            val imageRequest = coil.request.ImageRequest.Builder(context)
                .data(currentArtworkUri)
                .apply {
                    if (!isApi31AndAbove) {
                        transformations(com.example.claudwecho.ui.utils.BlurTransformation(context, 25f))
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
                        rotationZ = rotation
                        scaleX = scale
                        scaleY = scale
                    }
                    .then(if (isApi31AndAbove) Modifier.blur(60.dp) else Modifier)
            )
            // Add a dark overlay so text is readable
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        } else {
            // Default fluid gradient if no artwork
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotation
                        scaleX = scale
                        scaleY = scale
                    }
                    .blur(60.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.sweepGradient(
                            listOf(
                                Color(0xFF1E2124),
                                Color(0xFF3A3F47),
                                Color(0xFF2A2E35),
                                Color(0xFF1E2124)
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
                    onMenuClick = onNavigateToMenu,
                    onSettingsClick = onSettingsClick
                )
                1 -> LyricsScreen(viewModel = playerViewModel)
            }
        }
    }
}
