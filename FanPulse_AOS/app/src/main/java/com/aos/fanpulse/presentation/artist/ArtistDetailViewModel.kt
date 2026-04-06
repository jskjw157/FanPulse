package com.aos.fanpulse.presentation.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.repository.ArtistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val artistsRepository: ArtistsRepository,
): ContainerHost<ArtistDetailContract.ArtistDetailState, ArtistDetailContract.SideEffect>, ViewModel(){
    override val container: Container<ArtistDetailContract.ArtistDetailState, ArtistDetailContract.SideEffect> =
        container(initialState = ArtistDetailContract.ArtistDetailState(null))

    fun getArtistDetail(
        artistId: String,
    ) = intent {
        //  API 호출 전
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        Log.d("ArtistsViewModel", "API 호출 성공:${artistId}")
        val response = artistsRepository.getArtistDetail(artistId = artistId)
        if (response.isSuccessful) {
            val artistDetail = response.body()
            Log.d("ArtistsViewModel", "API 호출 성공: 아티스트 ${artistDetail}명 로드 완료")
            reduce {
                state.copy(
                    isLoading = false,
                    artistDetail = artistDetail
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