package com.aos.fanpulse.data.remote.apiservice

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

// 차트 전체 정보를 담는 모델
data class ChartDetail(
    val id: String,                 // 차트 ID (UUID)
    val chartType: String,          // 차트 종류 (예: DAILY, WEEKLY, REALTIME)
    val chartDate: String,          // 차트 기준 날짜 ("2026-04-01")
    val entries: List<ChartEntry>,  // 차트에 포함된 순위 리스트
    val createdAt: String           // 데이터 생성 일시
)

// 차트 내 개별 순위 항목
data class ChartEntry(
    val id: String,                 // 엔트리 고유 ID
    val rank: Int,                  // 현재 순위
    val trackId: String?,           // 곡 ID (곡 정보가 없는 차트일 경우 null 가능)
    val artistId: String,           // 아티스트 ID
    val trackTitle: String?,        // 곡 제목
    val artistName: String,         // 아티스트 이름
    val previousRank: Int,          // 이전 순위
    val peakRank: Int,              // 최고 순위
    val weeksOnChart: Int,          // 차트 진입 주차
    val rankChange: Int,            // 순위 변동폭 (예: +2, -1)
    val isNew: Boolean              // 신규 진입 여부
)

enum class ChartType {
    MELON, BUGS, GENIE, FLO, VIBE,
    BILLBOARD_KR, BILLBOARD_US,
    SPOTIFY, APPLE_MUSIC
}

data class ChartHistoryResponse(
    val content: List<ChartHistoryItem>
)

data class ChartHistoryItem(
    val id: String,                 // 특정 날짜의 차트 ID (상세 조회 시 사용)
    val chartType: String,          // 차트 플랫폼 (MELON 등)
    val chartDate: String,          // 차트 날짜 ("2026-04-01")
    val entryCount: Int             // 해당 차트에 등록된 순위 데이터 개수
)