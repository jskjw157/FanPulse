package com.aos.fanpulse.presentation.my

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyViewModel@Inject constructor(

) :  ViewModel() {

    data class MyItem(
        @DrawableRes val iconRes: Int,
        val title: String,
        val comment: String,
    )

    fun setMyItems() = listOf(
        MyItem(R.drawable.icon_liked_artist, "좋아요한 아티스트","likedartist"),
        MyItem(R.drawable.icon_bookmark, "저장한 게시물","bookmark"),
//        MyItem(R.drawable.icon_reservation, "예매 내역","reservation"),
//        MyItem(R.drawable.icon_setting, "설정","settings"),
//        MyItem(R.drawable.icon_supporting, "고객센터","supporting"),
    )

}