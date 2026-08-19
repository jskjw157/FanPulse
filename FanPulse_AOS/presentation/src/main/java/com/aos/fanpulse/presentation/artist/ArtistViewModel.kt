package com.aos.fanpulse.presentation.artist

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetArtistUseCase
import com.aos.fanpulse.domain.usecase.SearchArtistsUseCase
import com.aos.fanpulse.presentation.BuildConfig
import com.aos.fanpulse.presentation.R
import com.aos.fanpulse.presentation.common.FilterRadioButtonItem
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val getArtistUseCase: GetArtistUseCase,
    private val searchArtistsUseCase: SearchArtistsUseCase,
): ContainerHost<ArtistContract.ArtistState, ArtistContract.SideEffect>, ViewModel() {
    override val container: Container<ArtistContract.ArtistState, ArtistContract.SideEffect> =
        container(initialState = ArtistContract.ArtistState()){
            getArtists()
        }

    fun goArtistDetailScreen(artistId: String) = intent {
        postSideEffect(ArtistContract.SideEffect.NavigateArtistDetail(artistId))
    }

    fun setFilterRadioButtonItems() = listOf(
        FilterRadioButtonItem("전체", R.drawable.icon_artist_filter_total, true),
        FilterRadioButtonItem("보이그룹",R.drawable.icon_artist_filter_boy_group, false),
        FilterRadioButtonItem("걸그룹",R.drawable.icon_artist_filter_girl_group, false),
        FilterRadioButtonItem("솔로",R.drawable.icon_artist_filter_solo, false)
    )

    fun getArtists() = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val response = getArtistUseCase()
            if (response.isSuccess) {
                val artistsData = response.getOrNull()?.data?.content ?: emptyList()

                reduce {
                    state.copy(
                        isLoading = false,
                        artists = artistsData
                    )
                }
            } else {
                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "데이터를 불러오지 못했습니다.",
                        artists = emptyList()
                    )
                }
                handleErrorState("데이터를 불러오지 못했습니다.")
            }
        } catch (e: Exception) {
            handleErrorState("네트워크 연결 상태를 확인해주세요.")
        }
    }

    fun searchArtists(
        query: String,
        page: Int,
        size: Int
    ) = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        try {
            val response = runCatching {searchArtistsUseCase(query = query, page = page, size = size)}

            if (response.isSuccess) {
                val searchResults = response.getOrNull()?.data?.content ?:emptyList()
                reduce {
                    state.copy(
                        isLoading = false,
                        artists = searchResults

                    )
                }
            } else {
                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "검색 결과를 불러오지 못했습니다.",
                        artists = emptyList()
                    )
                }
            }
        } catch (e: Exception) {
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "네트워크 연결 상태를 확인해주세요.",
                    artists = emptyList()
                )
            }
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
                    artists = emptyList()
                )
            }
        }
    }
}