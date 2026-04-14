package com.aos.fanpulse.presentation.live

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetStreamingEventDetailUseCase
import com.aos.fanpulse.presentation.common.DummyData.streamingEventDetailDummyList
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LiveDetailViewModel@Inject constructor(
    private val getStreamingEventDetailUseCase: GetStreamingEventDetailUseCase
): ContainerHost<LiveDetailContract.LiveDetailState, LiveDetailContract.SideEffect>, ViewModel()  {
    override val container: Container<LiveDetailContract.LiveDetailState, LiveDetailContract.SideEffect> =
        container(initialState = LiveDetailContract.LiveDetailState( streamingEventDetailItem = streamingEventDetailDummyList[0])){}

    fun getLiveDetail (
        liveId: String,
    ) = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        val streamingEventDetail = getStreamingEventDetailUseCase(liveId)
        Log.d("LiveDetailViewModel", "API 호출 성공:${streamingEventDetail}")
        if (streamingEventDetail.isSuccessful){
            state.copy(
                isLoading = false,
                streamingEventDetailItem = streamingEventDetail.body()?.data ?: streamingEventDetailDummyList[0]
            )
        }else {
            // 실패 시
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다.",
                    streamingEventDetailItem = streamingEventDetailDummyList[0]
                )
            }
        }
    }
}