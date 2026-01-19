package com.aos.fanpulse.presentation

import androidx.compose.foundation.layout.Box
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
import com.aos.fanpulse.presentation.navigation.MainTabScreen
import com.aos.fanpulse.presentation.navigation.NavGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 배경 이미지를 전체에 깔아줍니다.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.loginscreen_bg),
                contentScale = ContentScale.Crop
            )
    ) {
        Scaffold(
            containerColor = when (currentRoute) {
                MainTabScreen.Home.route -> Color.Transparent
                MainTabScreen.Community.route -> Color.White
                else -> Color.Transparent
            },
            topBar = {
                when(currentRoute){
                    MainTabScreen.Home.route -> {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            title = {
                                IconButton(
                                    onClick = { /* 검색 클릭 이벤트 */ },
                                    modifier = Modifier
                                        .height(28.dp)
                                        .width(81.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.home_title),
                                        contentDescription = "검색",
                                        tint = Color.Unspecified
                                    )
                                }
                            },
                            actions = {
                                // 오른쪽 아이콘들 (순서대로 배치됨)
                                IconButton(onClick = { /* 검색 클릭 이벤트 */ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_search),
                                        contentDescription = "검색",
                                        tint = Color.Unspecified
                                    )
                                }
                                IconButton(onClick = { /* 알림 클릭 이벤트 */ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_alarm_inactive),
                                        contentDescription = "알림",
                                        tint = Color.Unspecified
                                    )
                                }
                                IconButton(onClick = { /* 설정 클릭 이벤트 */ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_inventory),
                                        contentDescription = "인벤",
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        )
                    }
                    MainTabScreen.Community.route -> {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            ),
                            title = {
                                Text(text = "Community")
                            },
                            actions = {
                                // 오른쪽 아이콘들 (순서대로 배치됨)
                                IconButton(onClick = { /* 검색 클릭 이벤트 */ }) {
                                    Icon(painter = painterResource(id = R.drawable.icon_search), contentDescription = "검색", tint = Color.Black)
                                }
                                IconButton(onClick = { /* 알림 클릭 이벤트 */ }) {
                                    Icon(painter = painterResource(id = R.drawable.icon_alarm_inactive), contentDescription = "알림", tint = Color.Black)
                                }
                            }
                        )
                    }
                    MainTabScreen.Live.route -> {

                    }
                    MainTabScreen.Voting.route -> {

                    }
                    MainTabScreen.My.route -> {

                    }
                }
            },
            bottomBar = {
                val isMainTab = MainTabScreen.tabItems.any { it.route == currentRoute }

                if (isMainTab) {
                    MyBottomNavigation(navController = navController)
                }
            }
        ) { innerPadding ->
            // 화면 이동 관리
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