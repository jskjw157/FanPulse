package com.aos.fanpulse.presentation.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.repository.ArtistsRepository
import com.aos.fanpulse.domain.usecase.GetNewsListUseCase
import com.aos.fanpulse.presentation.common.DummyData.artistDetailDummyList
import com.aos.fanpulse.presentation.common.DummyData.newsItemDummyList
import dagger.hilt.android.lifecycle.HiltViewModel
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
        container(initialState = ArtistDetailContract.ArtistDetailState(artistDetailDummyList[0], newsItemDummyList, newsItemDummyList))

    fun goNewsDetailScreen(newsId: String) = intent {
        postSideEffect(ArtistDetailContract.SideEffect.NavigateNewsDetail(newsId))
    }

    fun getArtistDetail(
        artistId: String,
    ) = intent {
        //  API 호출 전
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val getArtist = artistsRepository.getArtistDetail(artistId = artistId)
        val getNewsList = getNewsListUseCase.invoke(artistId, null, 20)
        val getScheduledList = getNewsListUseCase.invoke(artistId, null, 20)

        if (getArtist.isSuccessful && getNewsList.isSuccessful && getScheduledList.isSuccessful) {
            val artistDetail = getArtist.body()
            val newsList = getNewsList.body()
            val scheduledList = getScheduledList.body()
            Log.d("ArtistsViewModel", "API 호출 성공: 아티스트 ${artistDetail}명 로드 완료")
            Log.d("ArtistsViewModel", "API 호출 성공: ${newsList} 로드 완료")
            Log.d("ArtistsViewModel", "API 호출 성공: ${scheduledList} 로드 완료")

            reduce {
                state.copy(
                    isLoading = false,
                    artistDetail = artistDetail?: artistDetailDummyList[0],
                    newsItems = ( newsList?.content?: emptyList() ).ifEmpty { newsItemDummyList },
                    scheduledItems = ( scheduledList?.content?: emptyList() ).ifEmpty { newsItemDummyList },
                )
            }
        } else {
            // 실패 시
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다.",
                    artistDetail = artistDetailDummyList[0],
                    newsItems = newsItemDummyList ,
                    scheduledItems = newsItemDummyList ,
                )
            }
        }
    }
}