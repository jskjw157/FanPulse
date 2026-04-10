package com.aos.fanpulse.presentation.news

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetNewsDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val getNewsDetailUseCase: GetNewsDetailUseCase
): ContainerHost<NewsDetailContract.NewsDetailState, NewsDetailContract.SideEffect>, ViewModel(){
    override val container: Container<NewsDetailContract.NewsDetailState, NewsDetailContract.SideEffect> =
        container(initialState = NewsDetailContract.NewsDetailState(null))

    fun getNewsDetail(
        newsId: String,
    ) = intent {
        //  API 호출 전
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val getNewsDetail = getNewsDetailUseCase.invoke(newsId)

        if (getNewsDetail.isSuccessful) {
            val newsDetail = getNewsDetail.body()
            Log.d("ArtistsViewModel", "API 호출 성공: 아티스트 ${newsDetail}명 로드 완료")
            reduce {
                state.copy(
                    isLoading = false,
                    newsDetail = newsDetail,
                )
            }
        } else {
            // 실패 시
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다."
                )
            }
        }
    }
}