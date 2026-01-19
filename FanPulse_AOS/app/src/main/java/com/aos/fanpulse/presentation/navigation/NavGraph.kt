package com.aos.fanpulse.presentation.navigation

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.aos.fanpulse.presentation.community.CommunityScreen
import com.aos.fanpulse.presentation.home.HomeScreen
import com.aos.fanpulse.presentation.live.LiveScreen
import com.aos.fanpulse.presentation.login.LoginScreen
import com.aos.fanpulse.presentation.my.MyScreen
import com.aos.fanpulse.presentation.voting.VotingScreen

@Composable
fun NavGraph(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = SubScreen.Login.route,
){
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(innerPadding)
    ){
        composable(SubScreen.Login.route) {
            LoginScreen(){
                NavigationActions(navController).navigateHome()
            }
        }
        navigation(startDestination = MainTabScreen.Home.route, route = "main_tabs") {
            composable(MainTabScreen.Home.route) { HomeScreen() }
            composable(MainTabScreen.Community.route) {
                CommunityScreen(
                    { NavigationActions(navController).navigateCommunityPost() },
                    { NavigationActions(navController).navigateCommunityPostDetail() }
                )
            }
            composable(MainTabScreen.Live.route) { LiveScreen() }
            composable(MainTabScreen.Voting.route) { VotingScreen() }
            composable(MainTabScreen.My.route) { MyScreen() }
        }
    }
}