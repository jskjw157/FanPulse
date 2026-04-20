package com.aos.fanpulse.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
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
        val searchResult = searchAllUseCase(query = query, limit = limit)
        Log.d("SearchViewModel", "API 호출 성공:${searchResult}")
        if (searchResult.isSuccessful){
            reduce {
                state.copy(
                    isLoading = false,
                    liveItems = searchResult.body()!!.live.items,
                    newsItems = searchResult.body()!!.news.items,
                    totalLiveCount = searchResult.body()!!.live.totalCount,
                    totalNewsCount = searchResult.body()!!.news.totalCount
                )
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다.",
                    liveItems = liveItems,
                    newsItems = newsItems
                )
            }
        }
    }


}