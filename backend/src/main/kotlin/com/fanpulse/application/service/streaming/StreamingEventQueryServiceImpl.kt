package com.fanpulse.application.service.streaming

import com.fanpulse.application.dto.streaming.*
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
    private val streamingEventPort: StreamingEventPort
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
}
