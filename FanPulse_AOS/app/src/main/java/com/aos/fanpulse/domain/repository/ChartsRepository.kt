package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ChartDetail
import com.aos.fanpulse.data.remote.apiservice.ChartHistoryResponse
import retrofit2.Response

interface ChartsRepository {
    /**
     * 특정 차트 ID로 상세 데이터 조회 (이전 API)
     */
    suspend fun getChartDetail(chartId: String): Response<ChartDetail>

    /**
     * 특정 플랫폼의 최신 차트 데이터 조회
     */
    suspend fun getLatestChart(chartType: String): Response<ChartDetail>

    /**
     * 특정 플랫폼의 차트 이력 조회
     */
    suspend fun getChartHistory(
        chartType: String,
        startDate: String,
        endDate: String
    ): Response<ChartHistoryResponse>

    /**
     * 특정 플랫폼의 특정 날짜 차트 상세 조회
     */
    suspend fun getChartByDate(
        chartType: String,
        date: String
    ): Response<ChartDetail>
}