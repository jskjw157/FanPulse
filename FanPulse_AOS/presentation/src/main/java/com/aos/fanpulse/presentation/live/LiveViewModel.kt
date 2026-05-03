package com.aos.fanpulse.presentation.live

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetLiveEventsUseCase
import com.aos.fanpulse.domain.usecase.GetScheduledEventsUseCase
import com.aos.fanpulse.domain.usecase.GetStreamingEventsUseCase
import com.aos.fanpulse.presentation.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LiveViewModel@Inject constructor(
    private val getScheduledEventsUseCase: GetScheduledEventsUseCase,
    private val getStreamingEventsUseCase: GetStreamingEventsUseCase,
    private val getLiveEventsUseCase: GetLiveEventsUseCase,
): ContainerHost<LiveContract.LiveState, LiveContract.SideEffect>, ViewModel() {
    override val container: Container<LiveContract.LiveState, LiveContract.SideEffect> =
        container(
            initialState = LiveContract.LiveState()
        ) {
            getEvents()
        }

    fun getEvents() = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            coroutineScope {
                val streamEventsDeferred = async { getStreamingEventsUseCase.invoke() }
                val scheduledEventsDeferred = async { getScheduledEventsUseCase.invoke() }
                val liveEventsDeferred = async { getLiveEventsUseCase.invoke() }

                val streamResult = streamEventsDeferred.await()
                val scheduledResult = scheduledEventsDeferred.await()
                val liveResult = liveEventsDeferred.await()

//                Log.d("LiveViewModel", "API 호출 완료 - Stream:${streamResult.isSuccess}, Scheduled:${scheduledResult.isSuccess}, Live:${liveResult.isSuccess}")
                if (streamResult.isSuccess && scheduledResult.isSuccess && liveResult.isSuccess) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            streamingEventItem = streamResult.getOrNull()?.data?.items ?: emptyList(),
                            scheduledItem = scheduledResult.getOrNull()?.content ?: emptyList(),
                            liveItem = liveResult.getOrNull()?.content ?: emptyList()
                        )
                    }
                }else {
                    handleErrorState("일부 데이터를 불러오지 못했습니다.")
                }
            }
        } catch (e: Exception) {
//            Log.e("LiveViewModel", "API Exception", e)
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }
    private fun handleErrorState(message: String) = intent {
        if (BuildConfig.DEBUG) {
            // 디버그 빌드: UI 테스트를 위해 에러 메시지와 함께 더미 데이터 제공
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "[Debug] $message",
//                    streamingEventItem = streamingEventDummyList,
//                    scheduledItem = streamingEventSimpleDummyList,
//                    liveItem = streamingEventSimpleDummyList
                )
            }
        } else {
            // 릴리스 빌드: 더미 데이터 절대 노출 금지, 빈 리스트 + 에러 메시지
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = message,
                    streamingEventItem = emptyList(),
                    scheduledItem = emptyList(),
                    liveItem = emptyList()
                )
            }
        }
    }
    fun goLiveDetailScreen(liveId: String) = intent {
        postSideEffect(LiveContract.SideEffect.NavigateLiveDetail(liveId))
    }
}