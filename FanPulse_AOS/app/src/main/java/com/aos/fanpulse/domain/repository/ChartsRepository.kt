package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ChartDetail
import com.aos.fanpulse.data.remote.apiservice.ChartHistoryResponse
import com.aos.fanpulse.data.remote.apiservice.ChartsApiService
import retrofit2.Response
import javax.inject.Inject

class ChartsRepository @Inject constructor(
    private val apiService: ChartsApiService
) {
    /**
     * 특정 차트 ID로 상세 데이터 조회 (이전 API)
     * @param chartId 조회할 차트의 고유 ID
     */
    suspend fun getChartDetail(
        chartId: String
    ): Response<ChartDetail> {
        return apiService.getChartDetail(chartId)
    }

    /**
     * 특정 플랫폼의 최신 차트 데이터 조회
     * @param chartType 차트 플랫폼 종류 (예: MELON, SPOTIFY 등)
     */
    suspend fun getLatestChart(
        chartType: String
    ): Response<ChartDetail> {
        return apiService.getLatestChart(chartType)
    }

    /**
     * 특정 플랫폼의 차트 이력 조회
     * @param chartType 차트 플랫폼 종류
     * @param startDate 시작 날짜 (YYYY-MM-DD)
     * @param endDate 종료 날짜 (YYYY-MM-DD)
     */
    suspend fun getChartHistory(
        chartType: String,
        startDate: String,
        endDate: String
    ): Response<ChartHistoryResponse> {
        return apiService.getChartHistory(
            chartType = chartType,
            startDate = startDate,
            endDate = endDate
        )
    }

    /**
     * 특정 플랫폼의 특정 날짜 차트 상세 조회
     * @param chartType 차트 플랫폼 종류
     * @param date 조회하고 싶은 날짜 (YYYY-MM-DD 형식)
     */
    suspend fun getChartByDate(
        chartType: String,
        date: String
    ): Response<ChartDetail> {
        return apiService.getChartByDate(
            chartType = chartType,
            date = date
        )
    }
}