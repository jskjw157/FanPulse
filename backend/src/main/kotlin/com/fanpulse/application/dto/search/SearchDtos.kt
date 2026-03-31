package com.fanpulse.application.dto.search

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

/**
 * 라이브 이벤트와 뉴스 검색 결과를 통합한 응답 모델.
 */
@Schema(description = "Unified search response")
data class SearchResponse(
    @Schema(description = "Live (streaming events) search result")
    val live: SearchCategoryResponse<SearchLiveItem>,

    @Schema(description = "News search result")
    val news: SearchCategoryResponse<SearchNewsItem>
)

/**
 * 검색 결과의 카테고리별 항목 리스트와 전체 개수를 담는 제네릭 모델.
 */
@Schema(description = "Search result category")
data class SearchCategoryResponse<T>(
    @Schema(description = "Items in this category")
    val items: List<T>,

    @Schema(description = "Total matching items in this category")
    val totalCount: Long
)

/**
 * 통합 검색 결과에 포함되는 라이브 스트리밍 이벤트 항목.
 */
@Schema(description = "Live search item")
data class SearchLiveItem(
    @Schema(description = "Streaming event ID")
    val id: UUID,

    @Schema(description = "Event title")
    val title: String,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Artist name")
    val artistName: String,

    @Schema(description = "Thumbnail URL")
    val thumbnailUrl: String?,

    @Schema(description = "Event status", example = "LIVE")
    val status: String,

    @Schema(description = "Scheduled time")
    val scheduledAt: Instant
)

/**
 * 통합 검색 결과에 포함되는 뉴스 기사 항목.
 */
@Schema(description = "News search item")
data class SearchNewsItem(
    @Schema(description = "News ID")
    val id: UUID,

    @Schema(description = "News title")
    val title: String,

    @Schema(description = "News summary")
    val summary: String,

    @Schema(description = "News source name")
    val sourceName: String,

    @Schema(description = "Published timestamp")
    val publishedAt: Instant
)
