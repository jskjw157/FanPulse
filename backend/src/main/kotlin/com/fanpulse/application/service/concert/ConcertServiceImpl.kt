package com.fanpulse.application.service.concert

import com.fanpulse.domain.concert.CrawledConcert
import com.fanpulse.infrastructure.external.kopis.KopisConcertRecord
import com.fanpulse.infrastructure.external.kopis.KopisConcertSource
import com.fanpulse.infrastructure.external.kopis.KopisConcertSourceException
import com.fanpulse.infrastructure.persistence.concert.CrawledConcertJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Service
class ConcertSnapshotWriter(
    private val concerts: CrawledConcertJpaRepository,
) {
    @Transactional
    fun replace(records: List<KopisConcertRecord>, detailFailures: List<String>): ConcertSyncReport {
        require(records.isNotEmpty() && records.size <= 100) { "Concert snapshot size is invalid" }
        require(records.map { it.externalId }.toSet().size == records.size) {
            "Concert snapshot identifiers are duplicated"
        }
        val now = Instant.now()
        val existing = concerts.findAllBySource(CrawledConcert.SOURCE_KOPIS)
        val byExternalId = existing.associateBy { it.externalId }
        existing.forEach { it.deactivate(now) }
        records.forEach { record ->
            val entity = byExternalId[record.externalId]
            if (entity == null) {
                concerts.save(CrawledConcert.from(record, now))
            } else {
                entity.updateFrom(record, now)
            }
        }
        concerts.flush()
        return ConcertSyncReport(
            received = records.size,
            active = records.size,
            detailFailures = detailFailures.toList(),
        )
    }
}

@Service
class ConcertServiceImpl(
    private val source: KopisConcertSource,
    private val writer: ConcertSnapshotWriter,
    private val concerts: CrawledConcertJpaRepository,
) : ConcertService {
    override fun refreshFromSource(): ConcertSyncReport {
        val snapshot = source.fetchUpcomingPopularMusic(MAX_CONCERTS)
        if (snapshot.detailFailures.isNotEmpty()) {
            throw KopisConcertSourceException("KOPIS detail fetch failed")
        }
        return writer.replace(snapshot.records, snapshot.detailFailures)
    }

    @Transactional(readOnly = true)
    override fun getUpcoming(page: Int, size: Int): ConcertPageResponse {
        require(page >= 0) { "Page must not be negative" }
        require(size in 1..100) { "Page size must be between 1 and 100" }
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val result = concerts.findAllBySourceAndActiveTrueAndEndDateGreaterThanEqualOrderByStartDateAscExternalIdAsc(
            CrawledConcert.SOURCE_KOPIS,
            today,
            PageRequest.of(page, size),
        )
        return ConcertPageResponse(
            content = result.content.map(::toResponse),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            last = result.isLast,
        )
    }

    @Transactional(readOnly = true)
    override fun getById(id: UUID): ConcertResponse =
        concerts.findByIdAndSourceAndActiveTrue(id, CrawledConcert.SOURCE_KOPIS)?.let(::toResponse)
            ?: throw NoSuchElementException("공연을 찾을 수 없습니다")

    private fun toResponse(concert: CrawledConcert) = ConcertResponse(
        id = concert.id,
        externalId = concert.externalId,
        name = concert.name,
        artist = concert.artist,
        venueName = concert.venueName,
        venueHall = concert.venueHall,
        startDate = concert.startDate,
        endDate = concert.endDate,
        status = concert.status,
        posterUrl = concert.posterUrl,
        performanceTime = concert.performanceTime,
        priceText = concert.priceText,
        performers = concert.performers,
        runtime = concert.runtime,
        ageRating = concert.ageRating,
        venueAddress = concert.venueAddress,
        ticketUrl = concert.ticketUrl ?: concert.sourceUrl
            ?: throw IllegalStateException("공연 출처 링크가 없습니다"),
    )

    companion object {
        private const val MAX_CONCERTS = 60
    }
}
