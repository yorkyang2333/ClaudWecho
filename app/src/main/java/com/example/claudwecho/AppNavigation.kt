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
        composable("player?url={url}&title={title}") { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: "Unknown"
            
            PlayerScreen(
                onBack = {
                    navController.popBackStack()
                },
                url = url,
                title = title
            )
        }
    }
}
