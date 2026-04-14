package com.aos.fanpulse.presentation.news

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetNewsDetailUseCase
import com.aos.fanpulse.domain.usecase.GetNewsListUseCase
import com.aos.fanpulse.presentation.common.DummyData.newsDetailDummyList
import com.aos.fanpulse.presentation.common.DummyData.newsItemDummyList
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val getNewsDetailUseCase: GetNewsDetailUseCase,
    private val getNewsListUseCase: GetNewsListUseCase
): ContainerHost<NewsDetailContract.NewsDetailState, NewsDetailContract.SideEffect>, ViewModel(){
    override val container: Container<NewsDetailContract.NewsDetailState, NewsDetailContract.SideEffect> =
        container(initialState = NewsDetailContract.NewsDetailState(newsDetailDummyList[0]))

    fun getNewsDetail(
        newsId: String,
    ) = intent {
        //  API 호출 전
        reduce {
            state.copy(
                isLoading = false,
                errorMessage = null
            )
        }

        val getNewsDetail = getNewsDetailUseCase.invoke(newsId)
        Log.d("ArtistsViewModel", "API 호출 성공: 아티스트 ${getNewsDetail}명 로드 완료")

        if (getNewsDetail.isSuccessful) {
            val newsDetail = getNewsDetail.body()
            val getRelatedNews = getNewsListUseCase.invoke(
                newsDetail?.artistId
            )
            Log.d("NewsViewModel", "API 호출 성공:${getRelatedNews}")
            if (getRelatedNews.isSuccessful){
                reduce {
                    state.copy(
                        isLoading = false,
                        newsDetail = newsDetail?: newsDetailDummyList[0],
                        relatedNewsItem = (getRelatedNews.body()?.content?: emptyList()).ifEmpty {
                            newsItemDummyList
                        }
                    )
                }
            }
        } else {
            // 실패 시
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다.",
                    newsDetail = newsDetailDummyList[0],
                    relatedNewsItem = newsItemDummyList,
                )
            }
        }
    }
}