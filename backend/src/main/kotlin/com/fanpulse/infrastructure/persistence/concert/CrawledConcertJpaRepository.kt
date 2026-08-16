package com.fanpulse.infrastructure.persistence.concert

import com.fanpulse.domain.concert.CrawledConcert
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface CrawledConcertJpaRepository : JpaRepository<CrawledConcert, UUID> {
    fun findAllBySource(source: String): List<CrawledConcert>

    fun findBySourceAndExternalId(source: String, externalId: String): CrawledConcert?

    fun findByIdAndActiveTrue(id: UUID): CrawledConcert?

    fun findAllByActiveTrueAndEndDateGreaterThanEqualOrderByStartDateAscExternalIdAsc(
        endDate: LocalDate,
        pageable: Pageable,
    ): Page<CrawledConcert>
}
