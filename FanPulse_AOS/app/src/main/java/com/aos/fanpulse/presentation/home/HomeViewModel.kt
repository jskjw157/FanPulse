package com.aos.fanpulse.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.R
import com.aos.fanpulse.domain.repository.NewsRepository
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HomeViewModel@Inject constructor(
    private val streamingEventsRepository: StreamingEventsRepository,
    private val newsRepository: NewsRepository,
): ContainerHost<HomeContract.HomeState, HomeContract.SideEffect>, ViewModel() {

    fun setDrawerMenuItems() = listOf(
        MenuItem("artist", "아티스트", R.drawable.icon_menu_item_artist),
        MenuItem("chart", "차트", R.drawable.icon_menu_item_chart),
        MenuItem("news", "뉴스", R.drawable.icon_menu_item_news),
        MenuItem("concert", "콘서트", R.drawable.icon_menu_item_concert),
        MenuItem("tickets", "티켓", R.drawable.icon_menu_item_tickets),
        MenuItem("membership", "멤버십", R.drawable.icon_menu_item_membership),
        MenuItem("ads", "리워드", R.drawable.icon_menu_item_ads),
        MenuItem("favorites", "즐겨찾기", R.drawable.icon_menu_item_favorites),
        MenuItem("saved", "저장됨", R.drawable.icon_menu_item_saved),
        MenuItem("settings", "설정", R.drawable.icon_menu_item_settings),
        MenuItem("customer_service", "고객센터", R.drawable.icon_menu_item_customer_service),
    )

    override val container: Container<HomeContract.HomeState, HomeContract.SideEffect> =
        container(initialState = HomeContract.HomeState()){
            getHomeItems()
        }

    fun getHomeItems(

    ) = intent{
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val getStreamEvents = streamingEventsRepository.getStreamingEvents()
        Log.d("HomeViewModel", "API 호출 성공:${getStreamEvents}")
        val getScheduledEvents = streamingEventsRepository.getScheduledEvents()
        Log.d("HomeViewModel", "API 호출 성공:${getScheduledEvents}")
        val getLatestNews = newsRepository.getLatestNews(3)
        Log.d("HomeViewModel", "API 호출 성공:${getLatestNews}")

        if (getStreamEvents.isSuccessful && getScheduledEvents.isSuccessful ){
            reduce {
                state.copy(
                    isLoading = false,
                    streamingEventItem = getStreamEvents.body()?.data?.items ?: emptyList(),
                    scheduledItem = getScheduledEvents.body()?.content ?: emptyList(),
                    newsItem = getLatestNews.body()?.data ?: emptyList(),
                )
            }
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "데이터를 불러오는데 실패했습니다."
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
