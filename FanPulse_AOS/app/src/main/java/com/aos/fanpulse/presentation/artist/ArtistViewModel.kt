package com.aos.fanpulse.presentation.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.repository.ArtistsRepository
import com.aos.fanpulse.domain.usecase.SearchArtistsUseCase
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
        container(initialState = ArtistContract.ArtistState(emptyList()))

    fun goArtistDetailScreen(artistId: String) = intent {
        postSideEffect(ArtistContract.SideEffect.NavigateArtistDetail(artistId))
    }

    fun getArtists() = intent {
        //  API 호출 전
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        try {
            val response = artistsRepository.getArtists(
                activeOnly = true,
                page = 0,
                size = 20,
                sortBy = "name",
                sortDir = "asc"
            )
            if (response.isSuccessful) {
                val artists = response.body()?.content ?: emptyList()
                Log.d("ArtistsViewModel", "API 호출 성공: 아티스트 ${artists}명 로드 완료")
                reduce {
                    state.copy(
                        isLoading = false,
                        artists = artists
                    )
                }
            } else {
                Log.e("ArtistsViewModel", "API 호출 실패: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                // 실패 시
                reduce {
                    state.copy(
                        isLoading = false,
                        errorMessage = "데이터를 불러오는데 실패했습니다."
                    )
                }
            }
        }catch (e: Exception) {
            Log.e("ArtistsViewModel", "네트워크 예외 발생: ${e.message}", e)

            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = "네트워크 연결에 문제가 발생했습니다."
                )
            }
        }
    }

    fun searchArtists(
        query: String,
        page: Int,
        size: Int
    ) = intent{
        //  API 호출 전
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        val response = searchArtistsUseCase(query = query, page = page, size = size)
        if (response.isSuccessful) {
            val artists = response.body()?.content ?: emptyList()
            reduce {
                state.copy(
                    isLoading = false,
                    artists = artists
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