package com.aos.fanpulse.presentation.news

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.data.repository.NewsRepositoryImpl
import com.aos.fanpulse.domain.repository.NewsRepository
import com.aos.fanpulse.presentation.common.DummyData.newsDetailDummyList
import com.aos.fanpulse.presentation.common.FilterRadioButtonItem
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NewsViewModel@Inject constructor(
    private val newsRepository: NewsRepository,
): ContainerHost<NewsContract.NewsState, NewsContract.SideEffect>, ViewModel() {

    override val container: Container<NewsContract.NewsState, NewsContract.SideEffect> =
        container(initialState = NewsContract.NewsState()){
            getNewsItems()
        }

    fun setFilterRadioButtonItems() = listOf(
        FilterRadioButtonItem("전체", null, true),
        FilterRadioButtonItem("뉴스",null, false),
        FilterRadioButtonItem("공연",null, false),
        FilterRadioButtonItem("차트",null, false),
        FilterRadioButtonItem("발매",null, false),
    )

    fun goNewsDetail(newsId: String) = intent {
        postSideEffect(NewsContract.SideEffect.NavigateNewsDetail(newsId))
    }

    fun getNewsItems(

    ) = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        val getLatestNews = newsRepository.getLatestNews(3)
        Log.d("NewsViewModel", "API 호출 성공:${getLatestNews}")
        if ( getLatestNews.isSuccessful ){
            reduce {
                state.copy(
                    isLoading = false,
                    newsItem = (getLatestNews.body()?.data ?: emptyList())
                        .ifEmpty { newsDetailDummyList },
                )
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다.",
                    newsItem = newsDetailDummyList
                )
            }
        }
    }
}