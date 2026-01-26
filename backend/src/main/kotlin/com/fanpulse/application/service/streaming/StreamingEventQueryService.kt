package com.fanpulse.application.service.streaming

import com.fanpulse.application.dto.streaming.*
import com.fanpulse.domain.streaming.StreamingStatus
import org.springframework.data.domain.Pageable
import java.util.UUID

/**
 * Query service for streaming events.
 * Provides read-only operations for retrieving streaming event data.
 */
interface StreamingEventQueryService {

    /**
     * Gets a streaming event by ID.
     * @throws NoSuchElementException if event not found
     */
    fun getById(id: UUID): StreamingEventResponse

    /**
     * Gets all streaming events with pagination and filtering.
     */
    fun getAll(filter: StreamingEventFilter, pageable: Pageable): StreamingEventListResponse

    /**
     * Gets currently live streaming events.
     * Ordered by viewer count (descending).
     */
    fun getLive(pageable: Pageable): StreamingEventListResponse

    /**
     * Gets upcoming scheduled streaming events.
     * Ordered by scheduled time (ascending).
     */
    fun getScheduled(pageable: Pageable): StreamingEventListResponse

    /**
     * Gets streaming events by artist ID.
     */
    fun getByArtistId(artistId: UUID, pageable: Pageable): StreamingEventListResponse

    // === Cursor-based Pagination (MVP API Spec) ===

    /**
     * Gets streaming events with cursor-based pagination.
     * Returns list with artist names joined.
     *
     * @param status Optional status filter (LIVE/SCHEDULED/ENDED)
     * @param limit Number of items to fetch (1-50)
     * @param cursor Cursor for pagination (null for first page)
     * @return Cursor page response with items, nextCursor, and hasMore flag
     */
    fun getWithCursor(
        status: StreamingStatus?,
        limit: Int,
        cursor: String?
    ): CursorPageResponse<StreamingEventListItem>

    /**
     * Gets detailed streaming event by ID.
     * Includes artist name and formatted streamUrl.
     *
     * @param id Event ID
     * @return Detailed event response
     * @throws NoSuchElementException if event not found
     */
    fun getDetailById(id: UUID): StreamingEventDetailResponse
}
