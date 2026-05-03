package com.aos.fanpulse.data.remote.dto

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
