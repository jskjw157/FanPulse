package com.aos.fanpulse.data.remote.dto

// 전체 검색 결과 데이터
data class SearchResponse(
    val live: LiveSearchSection,
    val news: NewsSearchSection
)

// 라이브 검색 섹션
data class LiveSearchSection(
    val items: List<SearchLiveItem>,
    val totalCount: Int
)

// 뉴스 검색 섹션
data class NewsSearchSection(
    val items: List<SearchNewsItem>,
    val totalCount: Int
)

// 라이브 아이템 상세
data class SearchLiveItem(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val thumbnailUrl: String?,
    val status: String,             // 예: "LIVE", "SCHEDULED"
    val scheduledAt: String?
)

// 뉴스 아이템 상세 (이전 NewsItem과 필드 구성이 조금 다를 수 있으니 확인!)
data class SearchNewsItem(
    val id: String,
    val title: String,
    val summary: String?,           // 검색 결과에는 요약문(summary)이 포함됨
    val sourceName: String?,
    val publishedAt: String
)