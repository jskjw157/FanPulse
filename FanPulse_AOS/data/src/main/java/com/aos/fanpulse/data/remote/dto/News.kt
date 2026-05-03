package com.aos.fanpulse.data.remote.dto

// 뉴스 목록 응답 (페이징 포함)
data class NewsListResponse(
    val content: List<NewsItem>,
    val totalElements: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

data class BaseResponse<T>(
    val success: Boolean,
    val data: T
)

// 개별 뉴스 아이템
data class NewsItem(
    val id: String,                 // UUID
    val artistId: String,           // 해당 뉴스 관련 아티스트 ID
    val title: String,              // 뉴스 제목
    val thumbnailUrl: String?,      // 썸네일 이미지 URL
    val sourceName: String?,        // 언론사 또는 출처 (예: '중앙일보', 'Twitter')
    val category: String,           // 카테고리 (RELEASE, TOUR 등)
    val publishedAt: String         // 발행 시간 (ISO 8601 형식)
)

data class NewsDetail(
    val id: String,                 // 뉴스 ID (UUID)
    val artistId: String,           // 관련 아티스트 ID
    val title: String,              // 뉴스 제목
    val content: String,            // 뉴스 본문 내용 (HTML이나 마크다운일 가능성 있음)
    val sourceUrl: String?,         // 원문 기사 링크 URL
    val sourceName: String?,        // 출처 (예: 언론사명)
    val thumbnailUrl: String?,      // 썸네일 이미지 URL
    val category: String,           // 카테고리
    val viewCount: Int,             // 조회수
    val publishedAt: String,        // 발행 일시
    val createdAt: String           // 데이터 생성 일시
)