package com.example.claudwecho.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.claudwecho.ui.player.LyricsScreen
import com.example.claudwecho.ui.player.PlayerScreen
import com.example.claudwecho.ui.player.PlayerViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagerScreen(
    playerViewModel: PlayerViewModel,
    initialPage: Int = 0,
    onNavigateToLogin: () -> Unit,
    onNavigateToPlaylistDetail: (Long) -> Unit,
    onNavigateToFeature: (String) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 3 })

    var lastFlingTime by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0L) }

    HorizontalPager(
        state = pagerState,
        flingBehavior = androidx.compose.foundation.pager.PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = object : androidx.compose.foundation.pager.PagerSnapDistance {
                override fun calculateTargetPage(
                    startPage: Int,
                    suggestedTargetPage: Int,
                    velocity: Float,
                    pageSize: Int,
                    pageSpacing: Int
                ): Int {
                    val currentTime = System.currentTimeMillis()
                    val settled = pagerState.settledPage
                    
                    if (currentTime - lastFlingTime < 400) {
                        return settled
                    }
                    
                    val target = suggestedTargetPage.coerceIn(settled - 1, settled + 1)
                    if (target != settled) {
                        lastFlingTime = currentTime
                    }
                    return target
                }
            }
        )
    ) { page ->
        when (page) {
            0 -> MainScreen(
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToPlaylistDetail = onNavigateToPlaylistDetail,
                onNavigateToFeature = onNavigateToFeature
            )
            1 -> PlayerScreen(viewModel = playerViewModel)
            2 -> LyricsScreen(viewModel = playerViewModel)
        }
    }
}
