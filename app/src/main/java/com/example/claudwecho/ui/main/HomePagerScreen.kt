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
    onNavigateToMenu: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })

    HorizontalPager(
        state = pagerState
    ) { page ->
        when (page) {
            0 -> PlayerScreen(viewModel = playerViewModel, onMenuClick = onNavigateToMenu)
            1 -> LyricsScreen(viewModel = playerViewModel)
        }
    }
}
