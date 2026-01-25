package com.fanpulse.application.service.streaming

import com.fanpulse.application.dto.streaming.*
import com.fanpulse.domain.common.CursorPageRequest
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import com.fanpulse.infrastructure.common.PaginationConverter
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Implementation of StreamingEventQueryService.
 * Provides read-only operations for streaming event queries.
 * Converts Spring Pageable to Domain PageRequest for port calls.
 */
@Service
@Transactional(readOnly = true)
class StreamingEventQueryServiceImpl(
    private val streamingEventPort: StreamingEventPort,
    private val artistPort: ArtistPort
) : StreamingEventQueryService {

    override fun getById(id: UUID): StreamingEventResponse {
        logger.debug { "Getting streaming event by ID: $id" }
        val event = streamingEventPort.findEventById(id)
            ?: throw NoSuchElementException("Streaming event not found: $id")
        return StreamingEventResponse.from(event)
    }

    override fun getAll(filter: StreamingEventFilter, pageable: Pageable): StreamingEventListResponse {
        logger.debug { "Getting streaming events with filter: $filter" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = streamingEventPort.findWithFilters(
            status = filter.status,
            platform = filter.platform,
            artistId = filter.artistId,
            scheduledAfter = filter.scheduledAfter,
            scheduledBefore = filter.scheduledBefore,
            pageRequest = pageRequest
        )

        return StreamingEventListResponse(
            content = pageResult.content.map { StreamingEventSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }

    override fun getLive(pageable: Pageable): StreamingEventListResponse {
        logger.debug { "Getting live streaming events" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = streamingEventPort.findLiveOrderByViewerCountDesc(pageRequest)

        return StreamingEventListResponse(
            content = pageResult.content.map { StreamingEventSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }

    override fun getScheduled(pageable: Pageable): StreamingEventListResponse {
        logger.debug { "Getting scheduled streaming events" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = streamingEventPort.findScheduledOrderByScheduledAtAsc(pageRequest)

        return StreamingEventListResponse(
            content = pageResult.content.map { StreamingEventSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }

    override fun getByArtistId(artistId: UUID, pageable: Pageable): StreamingEventListResponse {
        logger.debug { "Getting streaming events for artist: $artistId" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = streamingEventPort.findByArtistId(artistId, pageRequest)

        return StreamingEventListResponse(
            content = pageResult.content.map { StreamingEventSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }

    // === Cursor-based Pagination (MVP API Spec) ===

    /**
     * Retrieves streaming events with cursor-based pagination, optionally filtered by status.
     *
     * This is the main MVP API endpoint for fetching streaming events. It uses cursor-based
     * pagination for efficient, stable pagination across large datasets.
     *
     * N+1 Query Prevention:
     * - Fetches all events in a single query to the repository
     * - Collects unique artist IDs from the results
     * - Performs a single batch query to fetch all artist names in one call
     * - Maps events to DTOs with pre-fetched artist names
     *
     * This prevents the N+1 query problem where a naive implementation would query the artist
     * table once per event in the result set.
     *
     * @param status Optional status filter (LIVE, SCHEDULED, or ENDED). When null, returns events of all statuses.
     * @param limit Number of items to return (validated to be between 1 and 50 by the controller).
     *   Capped at 50 by the API layer for reasonable response sizes.
     * @param cursor Base64-encoded cursor from a previous response, or null to start from the beginning
     *
     * @return [CursorPageResponse] containing:
     *   - items: List of streaming events with artist names joined
     *   - nextCursor: Encoded cursor to use for the next page (null if no more pages)
     *   - hasMore: Flag indicating whether additional pages exist
     *
     * @see StreamingEventPort.findWithCursor for database implementation details
     * @see ArtistPort.findNamesByIds for batch artist name fetching
     */
    override fun getWithCursor(
        status: StreamingStatus?,
        limit: Int,
        cursor: String?
    ): CursorPageResponse<StreamingEventListItem> {
        logger.debug { "Getting streaming events: status=$status, limit=$limit, cursor=$cursor" }

        val cursorRequest = CursorPageRequest.of(limit, cursor)
        val result = streamingEventPort.findWithCursor(status, limit, cursorRequest.cursor)

        // Batch fetch artist names to prevent N+1 query problem
        val artistIds = result.items.map { it.artistId }.distinct()
        val artistNames = artistPort.findNamesByIds(artistIds)

        // Map each StreamingEvent entity to StreamingEventListItem DTO
        val items = result.items.map { event ->
            StreamingEventListItem(
                id = event.id,
                title = event.title,
                artistId = event.artistId,
                artistName = artistNames[event.artistId] ?: "Unknown Artist",
                thumbnailUrl = event.thumbnailUrl,
                status = event.status.name,
                scheduledAt = event.scheduledAt,
                startedAt = event.startedAt,
                viewerCount = event.viewerCount
            )
        }

        return CursorPageResponse(
            items = items,
            nextCursor = result.nextCursor,
            hasMore = result.hasMore
        )
    }

    override fun getDetailById(id: UUID): StreamingEventDetailResponse {
        logger.debug { "Getting streaming event detail: $id" }

        val (event, artistName) = streamingEventPort.findByIdWithArtist(id)
            ?: throw NoSuchElementException("Streaming event not found: $id")

        return StreamingEventDetailResponse(
            id = event.id,
            title = event.title,
            description = event.description,
            artistId = event.artistId,
            artistName = artistName,
            thumbnailUrl = event.thumbnailUrl,
            streamUrl = formatStreamUrl(event.streamUrl),
            status = event.status.name,
            scheduledAt = event.scheduledAt,
            startedAt = event.startedAt,
            endedAt = event.endedAt,
            viewerCount = event.viewerCount,
            createdAt = event.createdAt
        )
    }

    /**
     * Formats stream URL with YouTube embed parameters for safe, consistent playback.
     *
     * Adds or preserves YouTube embed parameters that improve the embedded player experience:
     * - `rel=0`: Prevents related videos from recommended channels (focus on current stream)
     * - `modestbranding=1`: Hides YouTube logo for cleaner UI
     * - `playsinline=1`: Enables inline playback on iOS (not fullscreen by default)
     *
     * @param rawUrl Raw YouTube stream URL (may or may not contain parameters)
     * @return URL with embedded parameters appended (if not already present)
     */
    private fun formatStreamUrl(rawUrl: String): String {
        // If already has parameters, return as-is
        if (rawUrl.contains("?")) return rawUrl

        // Add YouTube embed parameters
        return "$rawUrl?rel=0&modestbranding=1&playsinline=1"
    }
}
