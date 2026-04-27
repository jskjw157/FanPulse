package com.aos.fanpulse.presentation.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.repository.ArtistsRepository
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
    private val artistsRepository: ArtistsRepository,
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
            val response = runCatching {
                artistsRepository.getArtists(
                    activeOnly = true,
                    page = 0,
                    size = 20,
                    sortBy = "name",
                    sortDir = "asc"
                )
            }
            if (response.isSuccess) {
                val artistsData = response.getOrNull()?.content ?: emptyList()
                Log.d("ArtistsViewModel", "API 호출 성공: 아티스트 ${artistsData.size}명 로드 완료")

                reduce {
                    state.copy(
                        isLoading = false,
                        artists = artistsData
                    )
                }
            } else {
//                Log.e("ArtistsViewModel", "API 호출 실패: HTTP ${response.code()}")
//                handleErrorState("데이터를 불러오지 못했습니다. (${response.code()})")
            }
        } catch (e: Exception) {
            Log.e("ArtistsViewModel", "네트워크 예외 발생", e)
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
                val searchResults = response.getOrNull()?.content ?: emptyList()
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
            Log.e("ArtistsViewModel", "검색 중 네트워크 예외 발생", e)
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
//                    artists = artistDummyList
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