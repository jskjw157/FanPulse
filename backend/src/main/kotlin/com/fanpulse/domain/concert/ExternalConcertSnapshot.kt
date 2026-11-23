package com.fanpulse.domain.concert

import java.time.LocalDate

data class ExternalConcertSnapshot(
    val externalId: String,
    val name: String,
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
