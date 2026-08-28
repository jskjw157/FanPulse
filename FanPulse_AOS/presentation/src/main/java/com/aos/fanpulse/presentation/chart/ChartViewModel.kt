package com.aos.fanpulse.presentation.chart

import androidx.lifecycle.ViewModel
import com.aos.fanpulse.domain.usecase.GetKoreaLastFmTopTracksUseCase
import com.aos.fanpulse.presentation.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val getKoreaLastFmTopTracksUseCase: GetKoreaLastFmTopTracksUseCase,
) : ContainerHost<ChartContract.ChartState, ChartContract.SideEffect>, ViewModel() {
    override val container: Container<ChartContract.ChartState, ChartContract.SideEffect> =
        container(
            initialState = ChartContract.ChartState()
        ) {
            getChartItems()
        }

    fun getChartItems() = intent {
        reduce {
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        try {
            coroutineScope {
                val lastFmTopTrackDeferred = async { getKoreaLastFmTopTracksUseCase() }
                val topTrackResult = lastFmTopTrackDeferred.await()
                if (topTrackResult.isSuccess) {
                    reduce {
                        state.copy(
                            isLoading = false,
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
                )
            }
        }
    }

}