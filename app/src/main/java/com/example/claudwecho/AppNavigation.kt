package com.example.claudwecho

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.claudwecho.ui.login.LoginScreen
import com.example.claudwecho.ui.main.MainScreen
import com.example.claudwecho.ui.player.PlayerScreen

import org.koin.androidx.compose.koinViewModel
import com.example.claudwecho.ui.player.PlayerViewModel
import com.example.claudwecho.ui.main.HomePagerScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberSwipeDismissableNavController()
) {
    val playerViewModel: PlayerViewModel = koinViewModel()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "main?page=0"
    ) {
        composable(
            route = "main?page={page}",
            arguments = listOf(
                androidx.navigation.navArgument("page") { type = androidx.navigation.NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val page = backStackEntry.arguments?.getInt("page") ?: 0
            HomePagerScreen(
                playerViewModel = playerViewModel,
                initialPage = page,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("main?page=0") { inclusive = true }
                    }
                },
                onNavigateToPlaylistDetail = { id ->
                    navController.navigate("playlist/$id")
                },
                onNavigateToFeature = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main?page=0") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("personal_fm") { DummyScreen("私人 FM", navController) }
        composable("daily_recommendation") { 
            val vm: com.example.claudwecho.ui.recommend.DailyRecommendViewModel = koinViewModel()
            com.example.claudwecho.ui.recommend.DailyRecommendScreen(
                viewModel = vm,
                onNavigateToPlayer = { pId, pTitle ->
                    playerViewModel.playSong(pId, pTitle)
                    navController.navigate("main?page=1") {
                        popUpTo("main?page=0") { inclusive = true }
                    }
                }
            )
        }
        
        composable("my_collection") { 
            val vm: com.example.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.example.claudwecho.ui.collection.MyCollectionScreen(
                viewModel = vm,
                onNavigateToPlaylists = { navController.navigate("my_collection/playlists") },
                onNavigateToAlbums = { navController.navigate("my_collection/albums") },
                onNavigateToBlogs = { navController.navigate("my_collection/blogs") }
            )
        }

        composable("my_collection/playlists") {
            // Need the same ViewModel instance, so we scope it or just let koinViewModel() create a new one since it caches the data via repository?
            // Wait, Koin ViewModel without scope might recreate it. That's fine for simple lists, but usually we'd scope it.
            // For now, koinViewModel() is okay.
            val vm: com.example.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.example.claudwecho.ui.collection.MyCollectionPlaylistsScreen(
                viewModel = vm,
                onNavigateToPlaylistDetail = { id ->
                    navController.navigate("playlist/$id")
                }
            )
        }

        composable("my_collection/albums") {
            val vm: com.example.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.example.claudwecho.ui.collection.MyCollectionAlbumsScreen(viewModel = vm)
        }

        composable("my_collection/blogs") {
            val vm: com.example.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.example.claudwecho.ui.collection.MyCollectionBlogsScreen(viewModel = vm)
        }
        composable("recently_played") { DummyScreen("最近播放", navController) }
        composable("settings") { DummyScreen("设置", navController) }
        
        composable(
            route = "playlist/{id}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            com.example.claudwecho.ui.playlist.PlaylistDetailScreen(
                playlistId = id,
                onNavigateToPlayer = { pId, pTitle ->
                    playerViewModel.playSong(pId, pTitle)
                    navController.navigate("main?page=1") {
                        popUpTo("main?page=0") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun DummyScreen(title: String, navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Back")
            }
        }
    }
}
