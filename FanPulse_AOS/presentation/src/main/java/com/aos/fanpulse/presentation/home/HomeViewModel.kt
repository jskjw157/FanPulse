package com.aos.fanpulse.presentation.home

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetCurrentUserIdUseCase
import com.aos.fanpulse.domain.usecase.GetKoreaLastFmTopTracksUseCase
import com.aos.fanpulse.domain.usecase.GetNewsLatestUseCase
import com.aos.fanpulse.domain.usecase.GetPostsUseCase
import com.aos.fanpulse.domain.usecase.GetScheduledEventsUseCase
import com.aos.fanpulse.domain.usecase.GetStreamingEventsUseCase
import com.aos.fanpulse.presentation.BuildConfig
import com.aos.fanpulse.presentation.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HomeViewModel@Inject constructor(
    private val getUserIdUseCase: GetCurrentUserIdUseCase,
    private val getScheduledEventsUseCase: GetScheduledEventsUseCase,
    private val getStreamingEventsUseCase: GetStreamingEventsUseCase,
    private val getNewsLatestUseCase: GetNewsLatestUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val getKoreaLastFmTopTracksUseCase: GetKoreaLastFmTopTracksUseCase,
): ContainerHost<HomeContract.HomeState, HomeContract.SideEffect>, ViewModel() {

    data class MenuItem(
        val id: String,
        val text: String,
        @DrawableRes val iconRes: Int
    )

    fun setDrawerMenuItems() = listOf(
        MenuItem("artist", "아티스트", R.drawable.icon_menu_item_artist),
        MenuItem("chart", "차트", R.drawable.icon_menu_item_chart),
        MenuItem("news", "뉴스", R.drawable.icon_menu_item_news),
        MenuItem("favorites", "즐겨찾기", R.drawable.icon_menu_item_favorites),
        MenuItem("saved", "저장됨", R.drawable.icon_menu_item_saved),
    )

    override val container: Container<HomeContract.HomeState, HomeContract.SideEffect> =
        container(
            initialState = HomeContract.HomeState()
        ) {
            getHomeItems()
        }

    fun getHomeItems() = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            coroutineScope {
                loadPosts(null)
                val streamEventsDeferred = async { getStreamingEventsUseCase() }
                val scheduledEventsDeferred = async { getScheduledEventsUseCase() }
                val latestNewsDeferred = async { getNewsLatestUseCase(3) }
                val lastFmTopTrackDeferred = async { getKoreaLastFmTopTracksUseCase(1,5) }

                val streamResult = streamEventsDeferred.await()
                val scheduledResult = scheduledEventsDeferred.await()
                val newsResult = latestNewsDeferred.await()
                val topTrackResult = lastFmTopTrackDeferred.await()

                if (streamResult.isSuccess && scheduledResult.isSuccess && newsResult.isSuccess && topTrackResult.isSuccess) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            streamingEventItem = streamResult.getOrNull()?.data?.items ?: emptyList(),
                            scheduledItem = scheduledResult.getOrNull()?.content ?: emptyList(),
                            newsItem = newsResult.getOrNull()?.data ?: emptyList(),
                            chartTracks = topTrackResult.getOrNull() ?: emptyList()
                        )
                    }
                } else {
                    handleErrorState("홈 화면 데이터를 불러오지 못했습니다.")
                }
            }
        } catch (e: Exception) {
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }
    fun loadPosts(artistCategory: String?) = intent {

        val myId = getUserIdUseCase()
        if (myId == null) {
            postSideEffect(HomeContract.SideEffect.ShowToast("로그인 정보가 없습니다."))
            return@intent
        }

        reduce { state.copy(isLoading = true, errorMessage = null) }

        getPostsUseCase(artistCategory,myId)
            .onSuccess { posts ->
                reduce {
                    state.copy(isLoading = false, posts = posts)
                }
            }
            .onFailure { exception ->
                reduce {
                    state.copy(isLoading = false, errorMessage = exception.toString())
                }
                postSideEffect(
                    HomeContract.SideEffect.ShowToast(
                        message = exception.message ?: "게시글을 불러오는 데 실패했습니다."
                    )
                )
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
                    streamingEventItem = emptyList(),
                    scheduledItem = emptyList(),
                    newsItem = emptyList()
                )
            }
        }
    }

    /**
    * 화면 이동
    * */
    fun goSearchScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateSearch)
    }

    fun goNotificationScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateNotification)
    }

    fun goArtistScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateArtist)
    }

    fun goCommunityScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateCommunity)
    }

    fun goCommunityDetailScreen(postId: String) = intent {
        postSideEffect(HomeContract.SideEffect.NavigateCommunityDetail(postId))
    }

    fun goChartScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateChart)
    }

    fun goNewsScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateNews)
    }
    fun goNewsDetailScreen(newsId: String) = intent {
        postSideEffect(HomeContract.SideEffect.NavigateNewsDetail(newsId))
    }

    fun goConcertScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateConcert)
    }

    fun goTicketsScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateTickets)
    }

    fun goMembershipScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateMembership)
    }

    fun goAdsScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateAds)
    }

    fun goFavoritesScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateFavorites)
    }

    fun goSavedScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateSaved)
    }

    fun goSettingsScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateSettings)
    }

    fun goSupportScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateSupport)
    }

    fun goLiveScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateLive)
    }

    fun goLiveDetailScreen(liveId: String) = intent {
        postSideEffect(HomeContract.SideEffect.NavigateLiveDetail(liveId))
    }
}
