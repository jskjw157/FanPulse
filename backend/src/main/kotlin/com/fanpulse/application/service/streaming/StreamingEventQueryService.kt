package com.fanpulse.application.service.streaming

import com.fanpulse.application.dto.streaming.*
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
}
