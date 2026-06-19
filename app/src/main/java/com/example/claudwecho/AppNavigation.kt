package com.example.claudwecho

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.claudwecho.ui.login.LoginScreen
import com.example.claudwecho.ui.main.MainScreen
import com.example.claudwecho.ui.player.PlayerScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberSwipeDismissableNavController()
) {
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onNavigateToPlayer = { url, title ->
                    // To keep it simple, we can navigate first, then let the PlayerViewModel know.
                    // A better way is using navigation arguments, but since PlayerViewModel is a singleton in Koin for now...
                    // Wait, PlayerViewModel is scoped to the ViewModel, so we can just retrieve it here.
                    navController.navigate("player?url=$url&title=$title")
                },
                onNavigateToPlaylistDetail = { id ->
                    navController.navigate("playlist/$id")
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "player?url={url}&title={title}",
            arguments = listOf(
                androidx.navigation.navArgument("url") { type = androidx.navigation.NavType.StringType; defaultValue = "" },
                androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType; defaultValue = "Unknown" }
            )
        ) { backStackEntry ->
            val url = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "Unknown", "UTF-8")
            
            PlayerScreen(
                onBack = {
                    navController.popBackStack()
                },
                url = url,
                title = title
            )
        }
        composable(
            route = "playlist/{id}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: return@composable
            com.example.claudwecho.ui.playlist.PlaylistDetailScreen(
                playlistId = id,
                onNavigateToPlayer = { pUrl, pTitle ->
                    navController.navigate("player?url=$pUrl&title=$pTitle")
                }
            )
        }
    }
}
