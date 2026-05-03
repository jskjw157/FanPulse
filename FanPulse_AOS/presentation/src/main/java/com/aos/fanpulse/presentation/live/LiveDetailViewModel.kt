package com.aos.fanpulse.presentation.live

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetStreamingEventDetailUseCase
import com.aos.fanpulse.presentation.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LiveDetailViewModel@Inject constructor(
    private val getStreamingEventDetailUseCase: GetStreamingEventDetailUseCase
): ContainerHost<LiveDetailContract.LiveDetailState, LiveDetailContract.SideEffect>, ViewModel()  {

    data class ChatMessage(
        val userName: String,
        val message: String,
        val timeAgo: String,
        val avatarInitial: String,
        val avatarColor: Color
    )

    data class ActionItem(
        val icon: @Composable () -> Unit,
        val count: String?,
        val label: String
    )

    val sampleChats = listOf(
        ChatMessage("민지팬123", "오늘 무대 최고에요! 🔥", "2분 전", "민", Color(0xFFAB47BC)),
        ChatMessage("하니러버",  "라이브 음색 미쳤다 ㅠㅠ",   "1분 전", "하", Color(0xFF42A5F5)),
        ChatMessage("뉴진스사랑","다들 너무 예뻐요 💕",       "방금",   "뉴", Color(0xFFEF5350))
    )

    override val container: Container<LiveDetailContract.LiveDetailState, LiveDetailContract.SideEffect> =
        container(
            initialState = LiveDetailContract.LiveDetailState()
        )

    fun getLiveDetail(liveId: String) = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val streamingEventDetail = getStreamingEventDetailUseCase(liveId)
//            Log.d("LiveDetailViewModel", "API 호출 성공:${streamingEventDetail}")
            if (streamingEventDetail.isSuccess) {
                val data = streamingEventDetail.getOrNull()?.data
                reduce {
                    state.copy(
                        isLoading = false,
                        streamingEventDetailItem = data,
                        errorMessage = if (data == null) "방송 정보를 찾을 수 없습니다." else null
                    )
                }
            } else {
                // 실패 시
                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "서버 응답 오류가 발생했습니다. ",//(${streamingEventDetail.getOrNull()?.getOrNull()?.code()})",
                        streamingEventDetailItem = null
                    )
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                reduce {
                    state.copy(
                        isLoading = false,
//                        streamingEventDetailItem = streamingEventDetailDummyList.firstOrNull(),
                        errorMessage = "[Debug] API 실패하여 더미 데이터를 표시합니다."
                    )
                }
            } else {
//                Log.e("LiveDetailViewModel", "API Exception", e)
                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "네트워크 연결 상태를 확인해주세요.",
                        streamingEventDetailItem = null
                    )
                }
            }
        }
    }
}