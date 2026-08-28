package com.aos.fanpulse.presentation.chart

import com.aos.fanpulse.domain.model.ChartTrack


object ChartContract{

    data class ChartState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val chartTracks: List<ChartTrack> = emptyList(),                    //  차트 순위
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect

    }
}