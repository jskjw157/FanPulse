package com.aos.fanpulse.presentation.artist

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.model.ArtistDetail
import com.aos.fanpulse.domain.usecase.GetArtistDetailUseCase
import com.aos.fanpulse.domain.usecase.GetNewsListUseCase
import com.aos.fanpulse.presentation.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val getArtistDetailUseCase: GetArtistDetailUseCase,
): ContainerHost<ArtistDetailContract.ArtistDetailState, ArtistDetailContract.SideEffect>, ViewModel(){
    override val container: Container<ArtistDetailContract.ArtistDetailState, ArtistDetailContract.SideEffect> =
        container(initialState = ArtistDetailContract.ArtistDetailState())

    fun goNewsDetailScreen(newsId: String) = intent {
        postSideEffect(ArtistDetailContract.SideEffect.NavigateNewsDetail(newsId))
    }

    fun getArtistDetail(artistId: String) = intent {

        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            coroutineScope {
                val artistDeferred = async { getArtistDetailUseCase(artistId) }

                val artistResult = artistDeferred.await()

                if (artistResult.success) {
                    val artistData = artistResult.data

                    reduce {
                        state.copy(
                            isLoading = false,
                            artistDetail = artistData,
                            errorMessage = null
                        )
                    }
                } else {
                    handleErrorState("아티스트 정보를 불러오지 못했습니다.")
                }
            }
        } catch (e: Exception) {
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }

    private fun handleErrorState(message: String) = intent {
        if (BuildConfig.DEBUG) {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "[Debug] $message",
                )
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = message,
                    artistDetail = null,
                    newsItems = emptyList(),
                    scheduledItems = emptyList()
                )
            }
        }
    }
}