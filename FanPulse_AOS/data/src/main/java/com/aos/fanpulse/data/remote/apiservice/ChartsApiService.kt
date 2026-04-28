package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.ChartDetail
import com.aos.fanpulse.data.remote.dto.ChartHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChartsApiService {


    /**
     * 특정 차트 ID로 상세 데이터 조회 (이전 API)
     */
    @GET("charts/{id}")
    suspend fun getChartDetail(
        @Path("id") chartId: String
    ): Response<ChartDetail>


    /**
     * 특정 플랫폼의 최신 차트 데이터 조회
     * @param chartType 차트 플랫폼 종류 (MELON, SPOTIFY 등)
     */
    @GET("charts/{chartType}/latest")
    suspend fun getLatestChart(
        @Path("chartType") chartType: String
    ): Response<ChartDetail>


    /**
     * 특정 플랫폼의 차트 이력 조회
     * @param chartType 차트 플랫폼 종류 (MELON, SPOTIFY 등)
     * @param startDate 시작 날짜 (YYYY-MM-DD)
     * @param endDate 종료 날짜 (YYYY-MM-DD)
     */
    @GET("charts/{chartType}/history")
    suspend fun getChartHistory(
        @Path("chartType") chartType: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<ChartHistoryResponse>


    /**
     * 특정 플랫폼의 특정 날짜 차트 상세 조회
     * @param chartType 차트 플랫폼 종류 (MELON, SPOTIFY 등)
     * @param date 조회하고 싶은 날짜 (YYYY-MM-DD 형식)
     */
    @GET("charts/{chartType}/date/{date}")
    suspend fun getChartByDate(
        @Path("chartType") chartType: String,
        @Path("date") date: String
    ): Response<ChartDetail>
}
