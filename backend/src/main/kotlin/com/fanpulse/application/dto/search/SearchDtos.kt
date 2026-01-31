package com.fanpulse.application.dto.search

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Unified search response")
data class SearchResponse(
    @Schema(description = "Live (streaming events) search result")
    val live: SearchCategoryResponse<SearchLiveItem>,

    @Schema(description = "News search result")
    val news: SearchCategoryResponse<SearchNewsItem>
)

@Schema(description = "Search result category")
data class SearchCategoryResponse<T>(
    @Schema(description = "Items in this category")
    val items: List<T>,

    @Schema(description = "Total matching items in this category")
    val totalCount: Long
)

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
