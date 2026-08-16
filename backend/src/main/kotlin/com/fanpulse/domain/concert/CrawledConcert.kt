package com.fanpulse.domain.concert

import com.fanpulse.infrastructure.external.kopis.KopisConcertRecord
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

import java.util.UUID

@Entity
@Table(name = "crawled_concerts")
class CrawledConcert(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "source", nullable = false, length = 30)
    val source: String,

    @Column(name = "external_id", nullable = false, length = 64)
    val externalId: String,

    @Column(name = "event_name", nullable = false, length = 255)
    var name: String,

    @Column(name = "artist", length = 1000)
    var artist: String? = null,

    @Column(name = "venue", length = 255)
    var venueName: String? = null,

    @Column(name = "venue_hall", length = 255)
    var venueHall: String? = null,

    @Column(name = "date")
    var legacyDate: LocalDateTime? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    @Column(name = "status", nullable = false, length = 30)
    var status: String,

    @Column(name = "ticket_link", length = 500)
    var ticketUrl: String? = null,

    @Column(name = "price_min", precision = 10, scale = 2)
    var priceMin: BigDecimal? = null,

    @Column(name = "price_max", precision = 10, scale = 2)
    var priceMax: BigDecimal? = null,

    @Column(name = "price_text", columnDefinition = "TEXT")
    var priceText: String? = null,

    @Column(name = "poster_url", columnDefinition = "TEXT")
    var posterUrl: String? = null,

    @Column(name = "performance_time", columnDefinition = "TEXT")
    var performanceTime: String? = null,

    @Column(name = "performers", columnDefinition = "TEXT")
    var performers: String? = null,

    @Column(name = "runtime", length = 100)
    var runtime: String? = null,

    @Column(name = "age_rating", length = 100)
    var ageRating: String? = null,

    @Column(name = "venue_address", length = 756)
    var venueAddress: String? = null,

    @Column(name = "source_url", length = 500)
    var sourceUrl: String? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @Column(name = "fetched_at", nullable = false)
    var fetchedAt: Instant = Instant.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    fun updateFrom(record: KopisConcertRecord, fetchedAt: Instant) {
        require(source == SOURCE_KOPIS && externalId == record.externalId)
        name = record.name
        artist = record.performers
        venueName = record.venueName
        venueHall = record.venueHall
        legacyDate = record.startDate.atStartOfDay()
        startDate = record.startDate
        endDate = record.endDate
        status = record.status
        ticketUrl = record.ticketUrl
        priceText = record.priceText
        posterUrl = record.posterUrl
        performanceTime = record.performanceTime
        performers = record.performers
        runtime = record.runtime
        ageRating = record.ageRating
        venueAddress = record.venueAddress
        sourceUrl = record.ticketUrl
        active = true
        this.fetchedAt = fetchedAt
        updatedAt = fetchedAt
    }

    fun deactivate(now: Instant) {
        active = false
        updatedAt = now
    }

    companion object {
        const val SOURCE_KOPIS = "KOPIS"

        fun from(record: KopisConcertRecord, now: Instant = Instant.now()) = CrawledConcert(
            source = SOURCE_KOPIS,
            externalId = record.externalId,
            name = record.name,
            artist = record.performers,
            venueName = record.venueName,
            venueHall = record.venueHall,
            legacyDate = record.startDate.atStartOfDay(),
            startDate = record.startDate,
            endDate = record.endDate,
            status = record.status,
            ticketUrl = record.ticketUrl,
            priceText = record.priceText,
            posterUrl = record.posterUrl,
            performanceTime = record.performanceTime,
            performers = record.performers,
            runtime = record.runtime,
            ageRating = record.ageRating,
            venueAddress = record.venueAddress,
            sourceUrl = record.ticketUrl,
            fetchedAt = now,
            createdAt = now,
            updatedAt = now,
        )
    }
}
