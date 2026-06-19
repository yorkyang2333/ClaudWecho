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
                onNavigateToPlayer = { id, title ->
                    navController.navigate("player?id=$id&title=$title")
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
            route = "player?id={id}&title={title}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType; defaultValue = 0L },
                androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType; defaultValue = "Unknown" }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "Unknown", "UTF-8")
            
            PlayerScreen(
                onBack = {
                    navController.popBackStack()
                },
                id = id,
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
                onNavigateToPlayer = { pId, pTitle ->
                    navController.navigate("player?id=$pId&title=$pTitle")
                }
            )
        }
    }
}
