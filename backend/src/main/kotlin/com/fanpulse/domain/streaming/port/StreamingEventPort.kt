package com.fanpulse.domain.streaming.port

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import java.time.Instant
import java.util.*

/**
 * Domain Port for StreamingEvent persistence.
 * This interface defines the contract for persistence operations
 * without any infrastructure dependencies.
 * 도메인 전용 Pagination 사용 (프레임워크 독립적)
 */
interface StreamingEventPort {

    fun findEventById(id: UUID): StreamingEvent?

    fun findByStatus(status: StreamingStatus): List<StreamingEvent>

    fun findByStatusNot(status: StreamingStatus): List<StreamingEvent>

    fun findByPlatformAndExternalId(platform: StreamingPlatform, externalId: String): StreamingEvent?

    /**
     * W1 Fix: Legacy 데이터 fallback 매칭용
     * platform/externalId가 없는 기존 데이터와 매칭하기 위해 streamUrl로 검색
     */
    fun findByStreamUrl(streamUrl: String): StreamingEvent?

    fun save(event: StreamingEvent): StreamingEvent

    // === Query Methods for REST API ===

    /**
     * Finds all events with pagination.
     */
    fun findAll(pageRequest: PageRequest): PageResult<StreamingEvent>

    /**
     * Finds events by status with pagination.
     */
    fun findByStatus(status: StreamingStatus, pageRequest: PageRequest): PageResult<StreamingEvent>

    /**
     * Finds live events ordered by viewer count (descending).
     */
    fun findLiveOrderByViewerCountDesc(pageRequest: PageRequest): PageResult<StreamingEvent>

    /**
     * Finds scheduled events ordered by scheduled time (ascending).
     */
    fun findScheduledOrderByScheduledAtAsc(pageRequest: PageRequest): PageResult<StreamingEvent>

    /**
     * Finds events by artist ID with pagination.
     */
    fun findByArtistId(artistId: UUID, pageRequest: PageRequest): PageResult<StreamingEvent>

    /**
     * Finds events with dynamic filters.
     */
    fun findWithFilters(
        status: StreamingStatus? = null,
        platform: StreamingPlatform? = null,
        artistId: UUID? = null,
        scheduledAfter: Instant? = null,
        scheduledBefore: Instant? = null,
        pageRequest: PageRequest
    ): PageResult<StreamingEvent>
}
