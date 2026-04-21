package com.aos.fanpulse.presentation.news

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.BuildConfig
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

    fun getNewsItems() = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val response = newsRepository.getLatestNews(3)
            Log.d("NewsViewModel", "API 호출 결과: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val data = response.body()?.data ?: emptyList()
                reduce {
                    state.copy(
                        isLoading = false,
                        newsItem = data,
                        errorMessage = if (data.isEmpty()) "최신 뉴스 소식이 없습니다." else null
                    )
                }
            } else {
                handleErrorState("뉴스 데이터를 불러오지 못했습니다. (${response.code()})")
            }
        } catch (e: Exception) {
            Log.e("NewsViewModel", "API Exception", e)
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }

    private fun handleErrorState(message: String) = intent {
        reduce {
            state.copy(
                isLoading = false,
                errorMessage = if (BuildConfig.DEBUG) "[Debug] $message" else message,
                newsItem = if (BuildConfig.DEBUG) newsDetailDummyList else emptyList()
            )
        }
    }
}