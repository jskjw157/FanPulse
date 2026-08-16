package com.fanpulse.application.service.concert

import java.time.LocalDate
import java.util.UUID

data class ConcertSyncReport(
    val received: Int,
    val active: Int,
    val detailFailures: List<String>,
)

data class ConcertResponse(
    val id: UUID,
    val externalId: String,
    val name: String,
    val artist: String?,
    val venueName: String?,
    val venueHall: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String,
    val posterUrl: String?,
    val performanceTime: String?,
    val priceText: String?,
    val performers: String?,
    val runtime: String?,
    val ageRating: String?,
    val venueAddress: String?,
    val ticketUrl: String,
)

data class ConcertPageResponse(
    val content: List<ConcertResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
)

interface ConcertService {
    fun refreshFromSource(): ConcertSyncReport
    fun getUpcoming(page: Int, size: Int): ConcertPageResponse
    fun getById(id: UUID): ConcertResponse
}
