package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * Spring Data JPA Repository for StreamingEvent.
 * Used by StreamingEventAdapter to implement the domain port.
 */
@Repository
interface StreamingEventJpaRepository : JpaRepository<StreamingEvent, UUID> {

    fun findByStatus(status: StreamingStatus): List<StreamingEvent>

    fun findByStatusNot(status: StreamingStatus): List<StreamingEvent>

    fun findByPlatformAndExternalId(platform: StreamingPlatform, externalId: String): StreamingEvent?

    fun findByStreamUrl(streamUrl: String): StreamingEvent?

    @Query("SELECT e FROM StreamingEvent e WHERE e.status = :status")
    fun findByStatusPaged(
        @Param("status") status: StreamingStatus,
        pageable: Pageable
    ): Page<StreamingEvent>

    @Query("SELECT e FROM StreamingEvent e WHERE e.status = 'LIVE' ORDER BY e.viewerCount DESC")
    fun findLiveOrderByViewerCountDesc(pageable: Pageable): Page<StreamingEvent>

    @Query("SELECT e FROM StreamingEvent e WHERE e.status = 'SCHEDULED' ORDER BY e.scheduledAt ASC")
    fun findScheduledOrderByScheduledAtAsc(pageable: Pageable): Page<StreamingEvent>

    @Query("SELECT e FROM StreamingEvent e WHERE e.artistId = :artistId")
    fun findByArtistId(
        @Param("artistId") artistId: UUID,
        pageable: Pageable
    ): Page<StreamingEvent>

    @Query("""
        SELECT e FROM StreamingEvent e
        WHERE (:status IS NULL OR e.status = :status)
        AND (:platform IS NULL OR e.platform = :platform)
        AND (:artistId IS NULL OR e.artistId = :artistId)
        AND (:scheduledAfter IS NULL OR e.scheduledAt >= :scheduledAfter)
        AND (:scheduledBefore IS NULL OR e.scheduledAt <= :scheduledBefore)
    """)
    fun findWithFilters(
        @Param("status") status: StreamingStatus?,
        @Param("platform") platform: StreamingPlatform?,
        @Param("artistId") artistId: UUID?,
        @Param("scheduledAfter") scheduledAfter: Instant?,
        @Param("scheduledBefore") scheduledBefore: Instant?,
        pageable: Pageable
    ): Page<StreamingEvent>

    /**
     * Unified search: search by event title OR artist name (case-insensitive).
     *
     * Note: StreamingEvent has only artistId, so we join via the Artist entity.
     */
    @Query("""
        SELECT e FROM StreamingEvent e, Artist a
        WHERE a.id = e.artistId
        AND e.status = :status
        AND (
            LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR (a.englishName IS NOT NULL AND LOWER(a.englishName) LIKE LOWER(CONCAT('%', :query, '%')))
        )
    """)
    fun searchByTitleOrArtistName(
        @Param("query") query: String,
        @Param("status") status: StreamingStatus,
        pageable: Pageable
    ): Page<StreamingEvent>
}
