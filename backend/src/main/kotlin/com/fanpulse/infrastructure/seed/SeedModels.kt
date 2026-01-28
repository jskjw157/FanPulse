package com.fanpulse.infrastructure.seed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedArtist(
    val name: String,
    val englishName: String? = null,
    val agency: String? = null,
    val isGroup: Boolean = false,
    val profileImageUrl: String? = null,
    val description: String? = null,
    val debutDate: Instant? = null,
    val active: Boolean? = null,
    val members: List<String>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedLiveEvent(
    val title: String,
    val description: String? = null,
    val artistId: UUID? = null,
    val artistName: String? = null,
    val streamUrl: String,
    val sourceUrl: String? = null,
    val thumbnailUrl: String? = null,
    val status: String = "SCHEDULED",
    val scheduledAt: Instant? = null,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val viewerCount: Int? = null,
    val platform: String? = null,
    val externalId: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedNews(
    val title: String,
    val content: String,
    val artistId: UUID? = null,
    val artistName: String? = null,
    val thumbnailUrl: String? = null,
    val url: String,
    val sourceName: String,
    val category: String? = null,
    val publishedAt: Instant
)
