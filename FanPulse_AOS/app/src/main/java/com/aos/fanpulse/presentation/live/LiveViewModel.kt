package com.aos.fanpulse.presentation.live

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LiveViewModel@Inject constructor(
    private val streamingEventsRepository: StreamingEventsRepository
): ContainerHost<LiveContract.LiveState, LiveContract.SideEffect>, ViewModel() {
    override val container: Container<LiveContract.LiveState, LiveContract.SideEffect> =
        container(initialState = LiveContract.LiveState()){
            getEvents()
        }

    fun getEvents (
    ) = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val getStreamEvents = streamingEventsRepository.getStreamingEvents()
        Log.d("LiveViewModel", "API 호출 성공:${getStreamEvents}")
        val getScheduledEvents = streamingEventsRepository.getScheduledEvents()
        Log.d("LiveViewModel", "API 호출 성공:${getScheduledEvents}")
        val getLiveEvents = streamingEventsRepository.getLiveEvents()
        Log.d("LiveViewModel", "API 호출 성공:${getLiveEvents}")
        if (getStreamEvents.isSuccessful && getScheduledEvents.isSuccessful && getLiveEvents.isSuccessful){
            reduce {
                state.copy(
                    isLoading = false,
                    streamingEventItem = getStreamEvents.body()?.data?.items ?: emptyList(),
                    scheduledItem = getScheduledEvents.body()?.content ?: emptyList(),
                    liveItem = getLiveEvents.body()?.content ?: emptyList(),
                )
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다."
                )
            }
        }
    }
}