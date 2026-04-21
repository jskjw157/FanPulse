package com.aos.fanpulse.presentation.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.BuildConfig
import com.aos.fanpulse.data.remote.apiservice.ArtistDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.domain.repository.ArtistsRepository
import com.aos.fanpulse.domain.usecase.GetNewsListUseCase
import com.aos.fanpulse.presentation.common.DummyData.artistDetailDummyList
import com.aos.fanpulse.presentation.common.DummyData.newsItemDummyList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val artistsRepository: ArtistsRepository,
    private val getNewsListUseCase: GetNewsListUseCase,
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
                val artistDeferred = async { artistsRepository.getArtistDetail(artistId) }
                val newsDeferred = async { getNewsListUseCase.invoke(artistId, null, 20) }
                val scheduledDeferred = async { getNewsListUseCase.invoke(artistId, null, 20) }

                val getArtist = artistDeferred.await()
                val getNewsList = newsDeferred.await()
                val getScheduledList = scheduledDeferred.await()

                Log.d("ArtistsViewModel", "API 호출 결과 - Artist:${getArtist.isSuccessful}, News:${getNewsList.isSuccessful}, Scheduled:${getScheduledList.isSuccessful}")

                if (getArtist.isSuccessful && getNewsList.isSuccessful && getScheduledList.isSuccessful) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            artistDetail = getArtist.body(),
                            newsItems = getNewsList.body()?.content ?: emptyList(),
                            scheduledItems = getScheduledList.body()?.content ?: emptyList(),
                            errorMessage = null
                        )
                    }
                } else {
                    handleErrorState("아티스트 정보를 불러오지 못했습니다.")
                }
            }
        } catch (e: Exception) {
            Log.e("ArtistsViewModel", "API Exception", e)
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }

    private fun handleErrorState(message: String) = intent {
        if (BuildConfig.DEBUG) {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "[Debug] $message",
                    artistDetail = artistDetailDummyList.firstOrNull(),
                    newsItems = newsItemDummyList,
                    scheduledItems = newsItemDummyList
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