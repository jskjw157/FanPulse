package com.aos.fanpulse.presentation.navigation

import androidx.annotation.DrawableRes
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
    object Voting : MainTabScreen("voting", "Voting", R.drawable.icon_voting)
    object My : MainTabScreen("my", "My", R.drawable.icon_my)

    companion object {
        val tabItems get() = listOf(Home, Community, Voting, My)
    }
}

sealed class SubScreen(route: String) : Screen(route) {
    object Login : SubScreen("login")
    object CommunityPost : SubScreen("community_post")
    object CommunityPostDetail : SubScreen("community_post_detail")
    object Settings : SubScreen("settings")
    object Error : SubScreen("error")
    object Search : SubScreen("search")
    object NewsDetail : SubScreen("news_detail")

    object Detail : SubScreen("detail/{taskId}?title={title}") {
        fun createRoute(taskId: Int, title: String): String {
            return "detail/$taskId?title=$title"
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
    fun navigateNewsDetail() {
        navController.navigate(SubScreen.NewsDetail.route) {
            launchSingleTop = true
        }
    }
}