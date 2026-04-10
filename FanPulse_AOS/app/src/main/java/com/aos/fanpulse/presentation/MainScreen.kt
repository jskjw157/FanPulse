package com.aos.fanpulse.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aos.fanpulse.R
import com.aos.fanpulse.navigation.MainTabScreen
import com.aos.fanpulse.navigation.NavGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 배경 이미지를 전체에 깔아줍니다.
    Box {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0), //  edge to edge
            bottomBar = {
                val isMainTab = MainTabScreen.tabItems.any { it.route == currentRoute }
                if (isMainTab) {
                    MyBottomNavigation(navController = navController)
                }
            }
        ) { innerPadding ->
            NavGraph(
                innerPadding = innerPadding,
                navController = navController
            )
        }
    }
}

@Composable
fun MyBottomNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.White
    ) {
        MainTabScreen.tabItems.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconRes),
                        contentDescription = screen.title,
                        tint = if (isSelected) colorResource(id = R.color.color_1) else colorResource(id = R.color.color_text_3)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 11.sp,
                        color = if (isSelected) colorResource(id = R.color.color_1) else colorResource(id = R.color.color_text_3)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = colorResource(id = R.color.color_1),
                    unselectedIconColor = colorResource(id = R.color.color_text_3),
                    selectedTextColor = colorResource(id = R.color.color_1),
                    unselectedTextColor = colorResource(id = R.color.color_text_3)
                )
            )
        }
    }
}