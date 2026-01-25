package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import com.fanpulse.infrastructure.common.PaginationConverter
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

    override fun searchByTitleOrArtistName(
        query: String,
        status: StreamingStatus,
        pageRequest: PageRequest
    ): PageResult<StreamingEvent> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.searchByTitleOrArtistName(query, status, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }
}
