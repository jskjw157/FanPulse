package com.aos.fanpulse.navigation

import android.net.http.SslCertificate.saveState
import androidx.annotation.DrawableRes
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.aos.fanpulse.R

sealed class Screen(val route: String)
sealed class MainTabScreen(
    route: String,
    val title: String,
    @DrawableRes val iconRes: Int
) : Screen(route) {
    object Home : MainTabScreen("home", "Home", R.drawable.icon_home)
    object Community : MainTabScreen("community", "Community", R.drawable.icon_community)
    object Live : MainTabScreen("live", "Live", R.drawable.icon_live)
//    object Voting : MainTabScreen("voting", "Voting", R.drawable.icon_voting)
    object My : MainTabScreen("my", "My", R.drawable.icon_my)

    companion object {
        val tabItems get() = listOf(Home, Community, Live, //Voting ,
        My)
    }
}

sealed class SubScreen(route: String) : Screen(route) {
    object Login : SubScreen("login")
    object CommunityPost : SubScreen("community_post")
    object CommunityPostDetail : SubScreen("community_post_detail")
    object Settings : SubScreen("settings")
    object Error : SubScreen("error")
    object Search : SubScreen("search")
//    object Voting : SubScreen("voting")
//    object Tickets : SubScreen("tickets")
    object News : SubScreen("news")
    object NewsDetail : SubScreen("news_detail/{newsId}"){
        fun createRoute(newsId: String): String {
            return "news_detail/$newsId"
        }
    }
//    object Membership : SubScreen("membership")
//    object TicketsDetail : SubScreen("tickets_detail")
    object Support : SubScreen("support")                       //  고객센터
    object Saved : SubScreen("saved")
    object Notifications : SubScreen("notifications")           //  알림
    object Favorites : SubScreen("favorites")
//    object Concert : SubScreen("concert")
//    object ConcertDetail : SubScreen("concert_detail")
    object Chart : SubScreen("chart")
//    object Ads : SubScreen("ads")
    object Artist : SubScreen("artist")
    object ArtistDetail : SubScreen("artist_detail/{artistId}") {
        fun createRoute(artistId: String): String {
            return "artist_detail/$artistId"
        }
    }
    object LiveDetail : SubScreen("live/{liveId}"){
        fun createRoute(liveId: String): String {
            return "live/$liveId"
        }
    }
}
class NavigationActions(private val navController: NavHostController){
    fun navigateHome() {
        navController.navigate(MainTabScreen.Home.route) {
            popUpTo(SubScreen.Login.route) { inclusive = true }
            launchSingleTop = true
        }
    }
    fun navigateLive() {
        navController.navigate(MainTabScreen.Live.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    fun navigateLiveDetail(liveId: String) {
        navController.navigate(SubScreen.LiveDetail.createRoute(liveId)) {
            launchSingleTop = true
        }
    }
    fun navigateCommunityPost(){
        navController.navigate(SubScreen.CommunityPost.route){
            launchSingleTop = true
        }
    }
    fun navigateCommunityPostDetail(){
        navController.navigate(SubScreen.CommunityPostDetail.route){
            launchSingleTop = true
        }
    }
    fun navigateSettings(){
        navController.navigate(SubScreen.Settings.route){
            launchSingleTop = true
        }
    }

    fun navigateError(){
        navController.navigate(SubScreen.Error.route){
            launchSingleTop = true
        }
    }

    fun navigateSearch(){
        navController.navigate(SubScreen.Search.route){
            launchSingleTop = true
        }
    }

//    fun navigateVoting() {
//        navController.navigate(SubScreen.Voting.route) {
//            launchSingleTop = true
//        }
//    }

//    fun navigateTickets() {
//        navController.navigate(SubScreen.Tickets.route) {
//            launchSingleTop = true
//        }
//    }
//    fun navigateMembership(){
//        navController.navigate(SubScreen.Membership.route) {
//            launchSingleTop = true
//        }
//    }

//    fun navigateTicketsDetail() {
//        navController.navigate(SubScreen.TicketsDetail.route) {
//            launchSingleTop = true
//        }
//    }

    fun navigateSupport() {
        navController.navigate(SubScreen.Support.route) {
            launchSingleTop = true
        }
    }

    fun navigateSaved() {
        navController.navigate(SubScreen.Saved.route) {
            launchSingleTop = true
        }
    }

    fun navigateNotifications() {
        navController.navigate(SubScreen.Notifications.route) {
            launchSingleTop = true
        }
    }

    fun navigateFavorites() {
        navController.navigate(SubScreen.Favorites.route) {
            launchSingleTop = true
        }
    }

//    fun navigateConcert() {
//        navController.navigate(SubScreen.Concert.route) {
//            launchSingleTop = true
//        }
//    }

//    fun navigateConcertDetail() {
//        navController.navigate(SubScreen.ConcertDetail.route) {
//            launchSingleTop = true
//        }
//    }

    fun navigateChart() {
        navController.navigate(SubScreen.Chart.route) {
            launchSingleTop = true
        }
    }
    fun navigateNews() {
        navController.navigate(SubScreen.News.route) {
            launchSingleTop = true
        }
    }

    fun navigateNewsDetail(newsId: String) {
        navController.navigate(SubScreen.NewsDetail.createRoute(newsId)) {
            launchSingleTop = true
        }
    }

//    fun navigateAds() {
//        navController.navigate(SubScreen.Ads.route) {
//            launchSingleTop = true
//        }
//    }

    fun navigateArtist() {
        navController.navigate(SubScreen.Artist.route) {
            launchSingleTop = true
        }
    }

    fun navigateArtistDetail(artistId: String) {
        navController.navigate(SubScreen.ArtistDetail.createRoute(artistId)) {
            launchSingleTop = true
        }
    }
}