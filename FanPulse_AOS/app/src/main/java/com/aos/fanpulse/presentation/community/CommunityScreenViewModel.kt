package com.aos.fanpulse.presentation.community

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommunityScreenViewModel@Inject constructor(

): ViewModel() {

    data class FilterRadioButtonItem(
        var text: String,
        var isSelected: Boolean,
    )

    fun setFilterRadioButtonItems() = listOf(
        FilterRadioButtonItem("Latest Posts", true),
        FilterRadioButtonItem("\uD83D\uDD25 Popular", false),
        FilterRadioButtonItem("Following", false)
    )
}