package com.fanpulse.application.dto.streaming

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

// === Response DTOs ===

@Schema(description = "Streaming event detail response")
data class StreamingEventResponse(
    @Schema(description = "Event ID")
    val id: UUID,

    @Schema(description = "Event title")
    val title: String,

    @Schema(description = "Event description")
    val description: String?,

    @Schema(description = "Streaming platform", example = "YOUTUBE")
    val platform: String?,

    @Schema(description = "External video ID")
    val externalId: String?,

    @Schema(description = "Embeddable stream URL")
    val streamUrl: String,

    @Schema(description = "Original source URL")
    val sourceUrl: String?,

    @Schema(description = "Thumbnail image URL")
    val thumbnailUrl: String?,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Scheduled start time")
    val scheduledAt: Instant,

    @Schema(description = "Actual start time (when went live)")
    val startedAt: Instant?,

    @Schema(description = "End time")
    val endedAt: Instant?,

    @Schema(description = "Current status", example = "LIVE")
    val status: String,

    @Schema(description = "Current viewer count")
    val viewerCount: Int,

    @Schema(description = "Event creation timestamp")
    val createdAt: Instant
) {
    companion object {
        fun from(event: StreamingEvent): StreamingEventResponse = StreamingEventResponse(
            id = event.id,
            title = event.title,
            description = event.description,
            platform = event.platform?.name,
            externalId = event.externalId,
            streamUrl = event.streamUrl,
            sourceUrl = event.sourceUrl,
            thumbnailUrl = event.thumbnailUrl,
            artistId = event.artistId,
            scheduledAt = event.scheduledAt,
            startedAt = event.startedAt,
            endedAt = event.endedAt,
            status = event.status.name,
            viewerCount = event.viewerCount,
            createdAt = event.createdAt
        )
    }
}

@Schema(description = "Streaming event summary for list views")
data class StreamingEventSummary(
    @Schema(description = "Event ID")
    val id: UUID,

    @Schema(description = "Event title")
    val title: String,

    @Schema(description = "Thumbnail image URL")
    val thumbnailUrl: String?,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Scheduled/started time")
    val scheduledAt: Instant,

    @Schema(description = "Current status")
    val status: String,

    @Schema(description = "Current viewer count (for LIVE events)")
    val viewerCount: Int,

    @Schema(description = "Platform")
    val platform: String?
) {
    companion object {
        fun from(event: StreamingEvent): StreamingEventSummary = StreamingEventSummary(
            id = event.id,
            title = event.title,
            thumbnailUrl = event.thumbnailUrl,
            artistId = event.artistId,
            scheduledAt = event.scheduledAt,
            status = event.status.name,
            viewerCount = event.viewerCount,
            platform = event.platform?.name
        )
    }
}

@Schema(description = "Paginated list of streaming events")
data class StreamingEventListResponse(
    @Schema(description = "List of events")
    val content: List<StreamingEventSummary>,

    @Schema(description = "Total number of events")
    val totalElements: Long,

    @Schema(description = "Current page number (0-based)")
    val page: Int,

    @Schema(description = "Page size")
    val size: Int,

    @Schema(description = "Total number of pages")
    val totalPages: Int
)

// === Cursor-based Pagination DTOs (for MVP API spec) ===

@Schema(description = "Standard API response wrapper")
data class ApiResponse<T>(
    @Schema(description = "Request success status")
    val success: Boolean,

    @Schema(description = "Response data")
    val data: T
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
    }
}

@Schema(description = "Cursor-based pagination response")
data class CursorPageResponse<T>(
    @Schema(description = "List of items")
    val items: List<T>,

    @Schema(description = "Cursor for next page (null if no more pages)")
    val nextCursor: String?,

    @Schema(description = "Whether more items exist")
    val hasMore: Boolean
)

@Schema(description = "Streaming event item for list views (with artist name)")
data class StreamingEventListItem(
    @Schema(description = "Event ID")
    val id: UUID,

    @Schema(description = "Event title")
    val title: String,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Artist name (joined from artists table)")
    val artistName: String,

    @Schema(description = "Thumbnail URL")
    val thumbnailUrl: String?,

    @Schema(description = "Event status", example = "LIVE")
    val status: String,

    @Schema(description = "Scheduled time")
    val scheduledAt: Instant,

    @Schema(description = "Actual start time (LIVE/ENDED only)")
    val startedAt: Instant?,

    @Schema(description = "Current viewer count")
    val viewerCount: Int
)

@Schema(description = "Streaming event detail (with artist name)")
data class StreamingEventDetailResponse(
    @Schema(description = "Event ID")
    val id: UUID,

    @Schema(description = "Event title")
    val title: String,

    @Schema(description = "Event description")
    val description: String?,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Artist name")
    val artistName: String,

    @Schema(description = "Thumbnail URL")
    val thumbnailUrl: String?,

    @Schema(description = "YouTube embed URL with parameters")
    val streamUrl: String,

    @Schema(description = "Event status")
    val status: String,

    @Schema(description = "Scheduled time")
    val scheduledAt: Instant,

    @Schema(description = "Actual start time")
    val startedAt: Instant?,

    @Schema(description = "End time")
    val endedAt: Instant?,

    @Schema(description = "Current viewer count")
    val viewerCount: Int,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant
)

// === Query Parameters ===

@Schema(description = "Filter criteria for streaming events")
data class StreamingEventFilter(
    @Schema(description = "Filter by status", example = "LIVE")
    val status: StreamingStatus? = null,

    @Schema(description = "Filter by platform", example = "YOUTUBE")
    val platform: StreamingPlatform? = null,

    @Schema(description = "Filter by artist ID")
    val artistId: UUID? = null,

    @Schema(description = "Events scheduled after this time")
    val scheduledAfter: Instant? = null,

    @Schema(description = "Events scheduled before this time")
    val scheduledBefore: Instant? = null
)
