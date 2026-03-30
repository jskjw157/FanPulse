package com.fanpulse.application.dto.content

import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

// === Response DTOs ===

@Schema(description = "News detail response")
data class NewsResponse(
    @Schema(description = "News ID")
    val id: UUID,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "News title")
    val title: String,

    @Schema(description = "News content")
    val content: String,

    @Schema(description = "Source URL")
    val sourceUrl: String,

    @Schema(description = "Source name")
    val sourceName: String,

    @Schema(description = "Thumbnail URL")
    val thumbnailUrl: String?,

    @Schema(description = "Category")
    val category: String,

    @Schema(description = "View count")
    val viewCount: Int,

    @Schema(description = "Published timestamp")
    val publishedAt: Instant,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant
) {
    companion object {
        fun from(news: News): NewsResponse = NewsResponse(
            id = news.id,
            artistId = news.artistId,
            title = news.title,
            content = news.content,
            sourceUrl = news.sourceUrl,
            sourceName = news.sourceName,
            thumbnailUrl = news.thumbnailUrl,
            category = news.category.name,
            viewCount = news.viewCount,
            publishedAt = news.publishedAt,
            createdAt = news.createdAt
        )
    }
}

@Schema(description = "News summary for list views")
data class NewsSummary(
    @Schema(description = "News ID")
    val id: UUID,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "News title")
    val title: String,

    @Schema(description = "Thumbnail URL")
    val thumbnailUrl: String?,

    @Schema(description = "Source name")
    val sourceName: String,

    @Schema(description = "Category")
    val category: String,

    @Schema(description = "Published timestamp")
    val publishedAt: Instant
) {
    companion object {
        fun from(news: News): NewsSummary = NewsSummary(
            id = news.id,
            artistId = news.artistId,
            title = news.title,
            thumbnailUrl = news.thumbnailUrl,
            sourceName = news.sourceName,
            category = news.category.name,
            publishedAt = news.publishedAt
        )
    }
}

@Schema(description = "Paginated list of news")
data class NewsListResponse(
    @Schema(description = "List of news")
    val content: List<NewsSummary>,

    @Schema(description = "Total number of news")
    val totalElements: Long,

    @Schema(description = "Current page number (0-based)")
    val page: Int,

    @Schema(description = "Page size")
    val size: Int,

    @Schema(description = "Total number of pages")
    val totalPages: Int
)

// === Filter ===

@Schema(description = "Filter criteria for news")
data class NewsFilter(
    @Schema(description = "Filter by artist ID")
    val artistId: UUID? = null,

    @Schema(description = "Filter by category")
    val category: NewsCategory? = null
)
