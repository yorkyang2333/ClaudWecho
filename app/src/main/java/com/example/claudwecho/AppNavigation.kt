package com.example.claudwecho

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.claudwecho.ui.login.LoginScreen
import com.example.claudwecho.ui.main.MainScreen

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
    }
}
