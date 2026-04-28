package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.domain.model.ChartDetail
import com.aos.fanpulse.domain.model.ChartHistoryResponse
import com.aos.fanpulse.data.remote.apiservice.ChartsApiService
import com.aos.fanpulse.domain.repository.ChartsRepository
import javax.inject.Inject

class ChartsRepositoryImpl @Inject constructor(
    private val apiService: ChartsApiService
) : ChartsRepository {
    /**
     * 특정 차트 ID로 상세 데이터 조회 (이전 API)
     * @param chartId 조회할 차트의 고유 ID -> UseCase 없음
     */
    override suspend fun getChartDetail(chartId: String): ChartDetail {
        val response = apiService.getChartDetail(chartId)
        if (response.isSuccessful) {
            // ChartDetail DTO -> Domain Model 변환
            return response.body()?.toDomain() ?: throw Exception("Chart detail not found")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 특정 플랫폼의 최신 차트 데이터 조회
     * @param chartType 차트 플랫폼 종류 (예: MELON, SPOTIFY 등) -> UseCase 없음
     */
    override suspend fun getLatestChart(chartType: String): ChartDetail {
        val response = apiService.getLatestChart(chartType)
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Latest chart not found")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 특정 플랫폼의 차트 이력 조회
     * @param chartType 차트 플랫폼 종류
     * @param startDate 시작 날짜 (YYYY-MM-DD)
     * @param endDate 종료 날짜 (YYYY-MM-DD)
     */
    override suspend fun getChartHistory(
        chartType: String,
        startDate: String,
        endDate: String
    ): ChartHistoryResponse {
        val response = apiService.getChartHistory(
            chartType = chartType,
            startDate = startDate,
            endDate = endDate
        )

        if (response.isSuccessful) {
            // ChartHistoryResponse DTO -> Domain Model 변환
            return response.body()?.toDomain() ?: throw Exception("History data empty")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 특정 플랫폼의 특정 날짜 차트 상세 조회
     * @param chartType 차트 플랫폼 종류
     * @param date 조회하고 싶은 날짜 (YYYY-MM-DD 형식)
     */
    override suspend fun getChartByDate(chartType: String, date: String): ChartDetail {
        val response = apiService.getChartByDate(
            chartType = chartType,
            date = date
        )

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Chart for date $date not found")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }
}