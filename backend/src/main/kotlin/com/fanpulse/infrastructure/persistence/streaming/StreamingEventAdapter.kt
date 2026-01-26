package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.common.CursorPageResult
import com.fanpulse.domain.common.DecodedCursor
import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import com.fanpulse.infrastructure.common.PaginationConverter
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * Adapter that implements StreamingEventPort using Spring Data JPA Repository.
 * Handles conversion between Domain pagination and Spring pagination.
 */
@Component
class StreamingEventAdapter(
    private val repository: StreamingEventJpaRepository
) : StreamingEventPort {

    override fun findEventById(id: UUID): StreamingEvent? {
        return repository.findById(id).orElse(null)
    }

    override fun findByStatus(status: StreamingStatus): List<StreamingEvent> {
        return repository.findByStatus(status)
    }

    override fun findByStatusNot(status: StreamingStatus): List<StreamingEvent> {
        return repository.findByStatusNot(status)
    }

    override fun findByPlatformAndExternalId(platform: StreamingPlatform, externalId: String): StreamingEvent? {
        return repository.findByPlatformAndExternalId(platform, externalId)
    }

    override fun findByStreamUrl(streamUrl: String): StreamingEvent? {
        return repository.findByStreamUrl(streamUrl)
    }

    override fun save(event: StreamingEvent): StreamingEvent {
        return repository.save(event)
    }

    override fun findAll(pageRequest: PageRequest): PageResult<StreamingEvent> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findAll(pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findByStatus(status: StreamingStatus, pageRequest: PageRequest): PageResult<StreamingEvent> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findByStatusPaged(status, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findLiveOrderByViewerCountDesc(pageRequest: PageRequest): PageResult<StreamingEvent> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findLiveOrderByViewerCountDesc(pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findScheduledOrderByScheduledAtAsc(pageRequest: PageRequest): PageResult<StreamingEvent> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findScheduledOrderByScheduledAtAsc(pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findByArtistId(artistId: UUID, pageRequest: PageRequest): PageResult<StreamingEvent> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findByArtistId(artistId, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findWithFilters(
        status: StreamingStatus?,
        platform: StreamingPlatform?,
        artistId: UUID?,
        scheduledAfter: Instant?,
        scheduledBefore: Instant?,
        pageRequest: PageRequest
    ): PageResult<StreamingEvent> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findWithFilters(
            status, platform, artistId, scheduledAfter, scheduledBefore, pageable
        )
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    // === Cursor-based Pagination (MVP API Spec) ===

    override fun findWithCursor(
        status: StreamingStatus?,
        limit: Int,
        cursor: DecodedCursor?
    ): CursorPageResult<StreamingEvent> {
        // Fetch limit + 1 to determine hasMore
        val fetchLimit = limit + 1
        val pageable = SpringPageRequest.of(0, fetchLimit)

        // Call the correct repository method based on cursor presence
        val events: List<StreamingEvent> = if (cursor == null) {
            // First page - no cursor
            repository.findFirstPageWithCursor(status, pageable)
        } else {
            // Next page - use cursor values
            val cursorScheduledAt = Instant.ofEpochMilli(cursor.scheduledAt)
            val cursorId = UUID.fromString(cursor.id)
            repository.findNextPageWithCursor(status, cursorScheduledAt, cursorId, pageable)
        }

        // Determine hasMore and build nextCursor
        val hasMore = events.size > limit
        val items = if (hasMore) events.dropLast(1) else events

        val nextCursor = if (hasMore && items.isNotEmpty()) {
            val last = items.last()
            DecodedCursor.from(last.scheduledAt, last.id).encode()
        } else {
            null
        }

        return CursorPageResult(items, nextCursor, hasMore)
    }

    override fun findByIdWithArtist(id: UUID): Pair<StreamingEvent, String>? {
        val result = repository.findByIdWithArtistName(id)
        if (result.isNullOrEmpty()) return null

        val row = result[0]
        val event = row[0] as StreamingEvent
        val artistName = (row[1] as? String) ?: "Unknown Artist"

        return Pair(event, artistName)
    }
}
