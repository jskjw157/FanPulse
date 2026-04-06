package com.aos.fanpulse.presentation.home

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel@Inject constructor(

): ViewModel() {

    fun setDrawerMenuItems() = listOf(
        MenuItem("artist", "아티스트", R.drawable.icon_menu_item_artist),
        MenuItem("chart", "차트", R.drawable.icon_menu_item_chart),
        MenuItem("news", "뉴스", R.drawable.icon_menu_item_news),
        MenuItem("concert", "콘서트", R.drawable.icon_menu_item_concert),
        MenuItem("tickets", "티켓", R.drawable.icon_menu_item_tickets),
        MenuItem("membership", "멤버십", R.drawable.icon_menu_item_membership),
        MenuItem("ads", "리워드", R.drawable.icon_menu_item_ads),
        MenuItem("favorites", "즐겨찾기", R.drawable.icon_menu_item_favorites),
        MenuItem("saved", "저장됨", R.drawable.icon_menu_item_saved),
        MenuItem("settings", "설정", R.drawable.icon_menu_item_settings),
        MenuItem("customer_service", "고객센터", R.drawable.icon_menu_item_customer_service),
    )
}
