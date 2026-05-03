package com.aos.fanpulse.presentation.home

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetNewsLatestUseCase
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
    private val getScheduledEventsUseCase: GetScheduledEventsUseCase,
    private val getStreamingEventsUseCase: GetStreamingEventsUseCase,
    private val getNewsLatestUseCase: GetNewsLatestUseCase,
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
//        MenuItem("concert", "콘서트", R.drawable.icon_menu_item_concert),
//        MenuItem("tickets", "티켓", R.drawable.icon_menu_item_tickets),
//        MenuItem("membership", "멤버십", R.drawable.icon_menu_item_membership),
//        MenuItem("ads", "리워드", R.drawable.icon_menu_item_ads),
        MenuItem("favorites", "즐겨찾기", R.drawable.icon_menu_item_favorites),
        MenuItem("saved", "저장됨", R.drawable.icon_menu_item_saved),
//        MenuItem("settings", "설정", R.drawable.icon_menu_item_settings),
//        MenuItem("customer_service", "고객센터", R.drawable.icon_menu_item_customer_service),
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
                val streamEventsDeferred = async { getStreamingEventsUseCase.invoke() }
                val scheduledEventsDeferred = async { getScheduledEventsUseCase.invoke() }
                val latestNewsDeferred = async { getNewsLatestUseCase.invoke(3) }

                val streamResult = streamEventsDeferred.await()
                val scheduledResult = scheduledEventsDeferred.await()
                val newsResult = latestNewsDeferred.await()

                if (streamResult.isSuccess && scheduledResult.isSuccess && newsResult.isSuccess) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            streamingEventItem = streamResult.getOrNull()?.data?.items ?: emptyList(),
                            scheduledItem = scheduledResult.getOrNull()?.content ?: emptyList(),
                            newsItem = newsResult.getOrNull()?.data ?: emptyList()
                        )
                    }
                } else {
                    handleErrorState("홈 화면 데이터를 불러오지 못했습니다.")
                }
            }
        } catch (e: Exception) {
//            Log.e("HomeViewModel", "API Exception", e)
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }

    private fun handleErrorState(message: String) = intent {
        if (BuildConfig.DEBUG) {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "[Debug] $message",
//                    streamingEventItem = streamingEventDummyList,
//                    scheduledItem = streamingEventSimpleDummyList,
//                    newsItem = newsDetailDummyList
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
    // 1. 단순 화면 이동 (인자 없음)
    fun goSearchScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateSearch)
    }

    fun goNotificationScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateNotification)
    }

    fun goArtistScreen() = intent {
        postSideEffect(HomeContract.SideEffect.NavigateArtist)
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
