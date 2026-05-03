package com.aos.fanpulse.domain.model

/*   커서 기반 응답 (기본 목록 및 상세)   */
// 기본 응답 래퍼
data class StreamingBaseResponse<T>(
    val success: Boolean,
    val data: T
)

// 메인 목록 데이터
data class StreamingEventCursorData(
    val items: List<StreamingEventItem>,
    val nextCursor: String?,
    val hasMore: Boolean
)

// 메인 목록 아이템
data class StreamingEventItem(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val thumbnailUrl: String,
    val status: String,
    val scheduledAt: String,
    val startedAt: String?,
    val viewerCount: Int
)

// 상세 정보 데이터
data class StreamingEventDetail(
    val id: String,
    val title: String,
    val description: String?,
    val artistId: String,
    val artistName: String,
    val thumbnailUrl: String,
    val streamUrl: String?,
    val status: String,
    val scheduledAt: String,
    val startedAt: String?,
    val endedAt: String?,
    val viewerCount: Int,
    val createdAt: String
)
/*   페이지 기반 응답 (Scheduled, Live, Legacy, Artist 전용)   */
data class StreamingPageResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

// 페이지 기반 목록 아이템 (공통)
data class StreamingEventSimpleItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val artistId: String,
    val scheduledAt: String,
    val status: String,
    val viewerCount: Int,
    val platform: String
)