package com.aos.fanpulse.presentation.news

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.CreateCommentUseCase
import com.aos.fanpulse.domain.usecase.GetNewsDetailUseCase
import com.aos.fanpulse.domain.usecase.GetNewsListUseCase
import com.aos.fanpulse.presentation.BuildConfig
import com.aos.fanpulse.presentation.community.CommunityDetailContract
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val getNewsDetailUseCase: GetNewsDetailUseCase,
    private val getNewsListUseCase: GetNewsListUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
): ContainerHost<NewsDetailContract.NewsDetailState, NewsDetailContract.SideEffect>, ViewModel(){
    override val container: Container<NewsDetailContract.NewsDetailState, NewsDetailContract.SideEffect> =
        container(
            initialState = NewsDetailContract.NewsDetailState()
        )

    fun getNewsDetail(newsId: String) = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        try {
            val detailResponse = getNewsDetailUseCase(newsId)
//            Log.d("NewsDetailViewModel", "API 호출 결과 (Detail): ${detailResponse.isSuccess}")

            if (detailResponse.isSuccess) {
                val newsDetailData = detailResponse.getOrNull()
//                Log.d("NewsDetailViewModel", "API 호출 결과 (Detail): ${newsDetailData}")
                if (newsDetailData != null) {
                    val relatedResponse = getNewsListUseCase(newsDetailData.artistId)
//                    Log.d("NewsDetailViewModel", "API 호출 결과 (Related): ${relatedResponse}")

                    val relatedNewsData = if (relatedResponse.isSuccess) {
                        relatedResponse.getOrNull()?.content ?: emptyList()
                    } else {
                        emptyList()
                    }

                    reduce {
                        state.copy(
                            isLoading = false,
                            newsDetail = newsDetailData,
                            relatedNewsItem = relatedNewsData
                        )
                    }
                } else {
                    handleErrorState("뉴스 상세 정보가 비어있습니다.")
                }
            } else {
                handleErrorState("뉴스 상세 정보를 불러오지 못했습니다.")
            }
        } catch (e: Exception) {
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }
    fun updateCommentInput(text: String) = intent {
        reduce { state.copy(commentInput = text) }
    }

    fun submitComment(postId: String) = intent {
        if (state.commentInput.isBlank()) return@intent

        reduce { state.copy(isLoading = true) }


        createCommentUseCase(
            postId = postId, content = state.commentInput
        ).onSuccess {
            reduce { state.copy(commentInput = "") }
            postSideEffect(NewsDetailContract.SideEffect.ShowToast("댓글이 등록되었습니다."))
        }.onFailure { exception ->
            reduce { state.copy(isLoading = false) }
            postSideEffect(NewsDetailContract.SideEffect.ShowToast("댓글 등록 실패: ${exception.message}"))
        }
        reduce { state.copy(commentInput = "", isLoading = false) }
    }
    private fun handleErrorState(message: String) = intent {
        if (BuildConfig.DEBUG) {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "[Debug] $message",
//                    newsDetail = newsDetailDummyList.firstOrNull(),
//                    relatedNewsItem = newsItemDummyList
                )
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = message,
                    newsDetail = null,
                    relatedNewsItem = emptyList()
                )
            }
        }
    }
}