package com.yorkyang2333.claudwecho

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
// removed import
import com.yorkyang2333.claudwecho.ui.main.MainScreen
import com.yorkyang2333.claudwecho.ui.player.PlayerScreen

import org.koin.androidx.compose.koinViewModel
import com.yorkyang2333.claudwecho.ui.player.PlayerViewModel
import com.yorkyang2333.claudwecho.ui.main.HomePagerScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberSwipeDismissableNavController()
) {
    val playerViewModel: PlayerViewModel = koinViewModel()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "player"
    ) {
        composable("player") {
            HomePagerScreen(
                playerViewModel = playerViewModel,
                initialPage = 0,
                onNavigateToMenu = {
                    navController.navigate("main")
                },
                onSettingsClick = {
                    navController.navigate("player_menu")
                }
            )
        }
        composable("main") { backStackEntry ->
            val mainViewModel: com.yorkyang2333.claudwecho.ui.main.MainViewModel = koinViewModel()
            
            val savedStateHandle = backStackEntry.savedStateHandle
            val loginSuccess = savedStateHandle.get<Boolean>("login_success")
            
            androidx.compose.runtime.LaunchedEffect(loginSuccess) {
                if (loginSuccess == true) {
                    mainViewModel.loadData(forceRefresh = true)
                    savedStateHandle.remove<Boolean>("login_success")
                }
            }

            MainScreen(
                viewModel = mainViewModel,
                onNavigateToLogin = {
                    navController.navigate("login")
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
            com.yorkyang2333.claudwecho.ui.login.LoginQrScreen(
                onLoginSuccess = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("login_success", true)
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }
        composable("liked") {
            val vm: com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailScreen(
                playlistId = -1L,
                type = "liked",
                viewModel = vm,
                onNavigateToPlayer = { songs, index ->
                    playerViewModel.playPlaylist(songs, index)
                    navController.navigate("player") {
                        popUpTo("player") { inclusive = true }
                    }
                }
            )
        }
        composable("personal_fm") {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                playerViewModel.playPersonalFm()
                navController.navigate("player") {
                    popUpTo("main")
                }
            }
        }
        composable("daily_recommendation") { 
            val vm: com.yorkyang2333.claudwecho.ui.recommend.DailyRecommendViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.recommend.DailyRecommendScreen(
                viewModel = vm,
                onNavigateToPlayer = { songs, index ->
                    playerViewModel.playPlaylist(songs, index)
                    navController.navigate("player") {
                        popUpTo("player") { inclusive = true }
                    }
                }
            )
        }
        composable("search") {
            val vm: com.yorkyang2333.claudwecho.ui.search.SearchViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.search.SearchScreen(
                viewModel = vm,
                playerViewModel = playerViewModel,
                onSongClick = {
                    navController.navigate("player") {
                        popUpTo("player") { inclusive = true }
                    }
                }
            )
        }
        
        composable("my_collection") { 
            val vm: com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.collection.MyCollectionScreen(
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
            val vm: com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.collection.MyCollectionPlaylistsScreen(
                viewModel = vm,
                onNavigateToPlaylistDetail = { id ->
                    navController.navigate("playlist/$id")
                }
            )
        }

        composable("my_collection/albums") {
            val vm: com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.collection.MyCollectionAlbumsScreen(
                viewModel = vm,
                onNavigateToAlbumDetail = { id -> navController.navigate("album/$id") }
            )
        }

        composable("my_collection/blogs") {
            val vm: com.yorkyang2333.claudwecho.ui.collection.MyCollectionViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.collection.MyCollectionBlogsScreen(
                viewModel = vm,
                onNavigateToDjRadioDetail = { id -> navController.navigate("djradio/$id") }
            )
        }
        composable("recently_played") { 
            val vm: com.yorkyang2333.claudwecho.ui.recent.RecentlyPlayedViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.recent.RecentlyPlayedScreen(
                viewModel = vm,
                onNavigateToPlayer = { songs, index ->
                    playerViewModel.playPlaylist(songs, index)
                    navController.navigate("player") {
                        popUpTo("player") { inclusive = true }
                    }
                }
            )
        }
        composable("profile") {
            val userProfileViewModel: com.yorkyang2333.claudwecho.ui.profile.UserProfileViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.profile.UserProfileScreen(
                viewModel = userProfileViewModel,
                onNavigateToLogin = {
                    try {
                        navController.getBackStackEntry("main").savedStateHandle.set("login_success", true)
                    } catch (e: Exception) {
                        // Ignore
                    }
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }
        composable("settings") { 
            val settingsViewModel: com.yorkyang2333.claudwecho.ui.settings.SettingsViewModel = koinViewModel()
            com.yorkyang2333.claudwecho.ui.settings.SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = false }
                    }
                }
            )
        }
        composable("player_menu") {
            com.yorkyang2333.claudwecho.ui.player.PlayerMenuScreen(viewModel = playerViewModel)
        }
        
        composable(
            route = "playlist/{id}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailScreen(
                playlistId = id,
                type = "playlist",
                onNavigateToPlayer = { songs, index ->
                    playerViewModel.playPlaylist(songs, index)
                    navController.navigate("player") {
                        popUpTo("player") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "album/{id}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailScreen(
                playlistId = id,
                type = "album",
                onNavigateToPlayer = { songs, index ->
                    playerViewModel.playPlaylist(songs, index)
                    navController.navigate("player") {
                        popUpTo("player") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "djradio/{id}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            com.yorkyang2333.claudwecho.ui.playlist.PlaylistDetailScreen(
                playlistId = id,
                type = "djradio",
                onNavigateToPlayer = { songs, index ->
                    playerViewModel.playPlaylist(songs, index)
                    navController.navigate("player") {
                        popUpTo("player") { inclusive = true }
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
