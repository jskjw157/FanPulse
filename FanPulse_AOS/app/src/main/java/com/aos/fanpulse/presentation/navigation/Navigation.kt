package com.aos.fanpulse.presentation.navigation

import androidx.navigation.NavHostController

sealed class Screen(
    val route: String
) {
    object Auth : Screen("auth")
    object Main : Screen("main")

    object Detail : Screen("detail/{taskId}?title={title}") {
        fun createRoute(
            taskId: Int,
            title: String
        ): String {
            return "detail/$taskId?title=$title"
        }
    }
}

class NavigationActions(private val navController: NavHostController){
    fun navigateMain() {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Auth.route) { inclusive = true }
            launchSingleTop = true
        }
    }
}