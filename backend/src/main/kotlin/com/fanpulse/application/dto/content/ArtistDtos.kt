package com.fanpulse.application.dto.content

import com.fanpulse.domain.content.Artist
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.util.*

// === Response DTOs ===

@Schema(description = "Artist detail response")
data class ArtistResponse(
    @Schema(description = "Artist ID")
    val id: UUID,

    @Schema(description = "Artist name")
    val name: String,

    @Schema(description = "English name")
    val englishName: String?,

    @Schema(description = "Agency")
    val agency: String?,

    @Schema(description = "Description")
    val description: String?,

    @Schema(description = "Profile image URL")
    val profileImageUrl: String?,

    @Schema(description = "Whether this is a group")
    val isGroup: Boolean,

    @Schema(description = "Group members (if group)")
    val members: Set<String>,

    @Schema(description = "Whether artist is active")
    val active: Boolean,

    @Schema(description = "Debut date")
    val debutDate: LocalDate?,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant
) {
    companion object {
        fun from(artist: Artist): ArtistResponse = ArtistResponse(
            id = artist.id,
            name = artist.name,
            englishName = artist.englishName,
            agency = artist.agency,
            description = artist.description,
            profileImageUrl = artist.profileImageUrl,
            isGroup = artist.isGroup,
            members = artist.members,
            active = artist.active,
            debutDate = artist.debutDate,
            createdAt = artist.createdAt
        )
    }
}

@Schema(description = "Artist summary for list views")
data class ArtistSummary(
    @Schema(description = "Artist ID")
    val id: UUID,

    @Schema(description = "Artist name")
    val name: String,

    @Schema(description = "English name")
    val englishName: String?,

    @Schema(description = "Agency")
    val agency: String?,

    @Schema(description = "Profile image URL")
    val profileImageUrl: String?,

    @Schema(description = "Whether this is a group")
    val isGroup: Boolean
) {
    companion object {
        fun from(artist: Artist): ArtistSummary = ArtistSummary(
            id = artist.id,
            name = artist.name,
            englishName = artist.englishName,
            agency = artist.agency,
            profileImageUrl = artist.profileImageUrl,
            isGroup = artist.isGroup
        )
    }
}

@Schema(description = "Paginated list of artists")
data class ArtistListResponse(
    @Schema(description = "List of artists")
    val content: List<ArtistSummary>,

    @Schema(description = "Total number of artists")
    val totalElements: Long,

    @Schema(description = "Current page number (0-based)")
    val page: Int,

    @Schema(description = "Page size")
    val size: Int,

    @Schema(description = "Total number of pages")
    val totalPages: Int
)
