package com.aos.fanpulse.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.aos.fanpulse.presentation.ads.AdsScreen
import com.aos.fanpulse.presentation.artist.ArtistDetailScreen
import com.aos.fanpulse.presentation.artist.ArtistScreen
import com.aos.fanpulse.presentation.chart.ChartScreen
import com.aos.fanpulse.presentation.community.CommunityPostDetailScreen
import com.aos.fanpulse.presentation.community.CommunityPostScreen
import com.aos.fanpulse.presentation.community.CommunityScreen
import com.aos.fanpulse.presentation.concert.ConcertDetailScreen
import com.aos.fanpulse.presentation.concert.ConcertScreen
import com.aos.fanpulse.presentation.error.ErrorScreen
import com.aos.fanpulse.presentation.favorites.FavoritesScreen
import com.aos.fanpulse.presentation.home.HomeScreen
import com.aos.fanpulse.presentation.login.LoginScreen
import com.aos.fanpulse.presentation.my.MyScreen
import com.aos.fanpulse.presentation.news.NewsDetailScreen
import com.aos.fanpulse.presentation.notifications.NotificationsScreen
import com.aos.fanpulse.presentation.saved.SavedScreen
import com.aos.fanpulse.presentation.search.SearchScreen
import com.aos.fanpulse.presentation.settings.SettingsScreen
import com.aos.fanpulse.presentation.support.SupportScreen
import com.aos.fanpulse.presentation.tickets.TicketsScreen
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
        navigation(startDestination = MainTabScreen.Home.route, route = "main_tab") {
            composable(MainTabScreen.Home.route) { HomeScreen() }
            composable(MainTabScreen.Community.route) {
                CommunityScreen(
                    { NavigationActions(navController).navigateCommunityPost() },
                    { NavigationActions(navController).navigateCommunityPostDetail() }
                )
            }
            composable(MainTabScreen.Voting.route) { VotingScreen() }
            composable(MainTabScreen.My.route) { MyScreen(
                { NavigationActions(navController).navigateSettings() }
            )}
        }

        composable(SubScreen.CommunityPost.route) {
            CommunityPostScreen({
                navController.popBackStack()
            },{

            })
        }

        composable(SubScreen.CommunityPostDetail.route) {
            CommunityPostDetailScreen({
                navController.popBackStack()
            },{})
        }

        composable (SubScreen.Settings.route){
            SettingsScreen({
                navController.popBackStack()
            })
        }

        composable (SubScreen.Error.route){
            ErrorScreen()
        }

        composable (SubScreen.Search.route){
            SearchScreen()
        }

        composable (SubScreen.NewsDetail.route){
            NewsDetailScreen()
        }
        composable(SubScreen.Voting.route) {
            VotingScreen()
        }

        composable(SubScreen.Tickets.route) {
            TicketsScreen()
        }

//        composable(SubScreen.TicketsDetail.route) {
//            TicketsDetailScreen()
//        }

        composable(SubScreen.Support.route) {
            SupportScreen()
        }

        composable(SubScreen.Saved.route) {
            SavedScreen()
        }

        composable(SubScreen.Notifications.route) {
            NotificationsScreen()
        }

        composable(SubScreen.Favorites.route) {
            FavoritesScreen()
        }

        composable(SubScreen.Concert.route) {
            ConcertScreen()
        }

        composable(SubScreen.ConcertDetail.route) {
            ConcertDetailScreen()
        }

        composable(SubScreen.Chart.route) {
            ChartScreen()
        }

        composable(SubScreen.Ads.route) {
            AdsScreen()
        }

        composable(SubScreen.Artist.route) {
            ArtistScreen()
        }

        composable(SubScreen.ArtistDetail.route) {
            ArtistDetailScreen()
        }
    }
}