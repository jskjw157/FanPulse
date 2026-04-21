package com.aos.fanpulse.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.BuildConfig
import com.aos.fanpulse.domain.usecase.SearchAllUseCase
import com.aos.fanpulse.presentation.common.DummyData.liveItems
import com.aos.fanpulse.presentation.common.DummyData.newsItems
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchAllUseCase : SearchAllUseCase
): ContainerHost<SearchContract.SearchState, SearchContract.SideEffect>, ViewModel(){
    override val container: Container<SearchContract.SearchState, SearchContract.SideEffect> =
        container(initialState = SearchContract.SearchState())

    data class RecentSearchTag(
        val text: String,
    )

    data class PopularSearch(
        val rank: Int,
        val text: String
    )

    fun deleteAllRecentSearch() = intent {
        reduce {
            state.copy(
                recentSearchTags = emptyList()
            )
        }
    }

    fun deleteRecentSearch(tag: RecentSearchTag) = intent {
        reduce {
            state.copy(
                recentSearchTags = state.recentSearchTags.filterNot { it == tag }
            )
        }
    }

    fun getSearchResult(
        query: String,
        limit: Int = 10
    ) = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val searchResult = searchAllUseCase(query = query, limit = limit)
            Log.d("SearchViewModel", "API 호출 결과: ${searchResult.isSuccessful}")

            if (searchResult.isSuccessful) {
                val body = searchResult.body()

                reduce {
                    state.copy(
                        isLoading = false,
                        liveItems = body?.live?.items ?: emptyList(),
                        newsItems = body?.news?.items ?: emptyList(),
                        totalLiveCount = body?.live?.totalCount ?: 0,
                        totalNewsCount = body?.news?.totalCount ?: 0
                    )
                }
            } else {
                Log.e("SearchViewModel", "검색 API 실패: HTTP ${searchResult.code()}")
                handleErrorState("검색 결과를 불러오지 못했습니다. (${searchResult.code()})")
            }
        } catch (e: Exception) {
            Log.e("SearchViewModel", "검색 중 네트워크 예외 발생", e)
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }

    private fun handleErrorState(message: String) = intent {
        if (BuildConfig.DEBUG) {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "[Debug] $message",
                    liveItems = liveItems,
                    newsItems = newsItems,
                    totalLiveCount = liveItems.size,
                    totalNewsCount = newsItems.size
                )
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = message,
                    liveItems = emptyList(),
                    newsItems = emptyList(),
                    totalLiveCount = 0,
                    totalNewsCount = 0
                )
            }
        }
    }
}