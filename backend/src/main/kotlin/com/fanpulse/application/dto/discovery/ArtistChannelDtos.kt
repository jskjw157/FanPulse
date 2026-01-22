package com.fanpulse.application.dto.discovery

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.streaming.StreamingPlatform
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

// === Response DTOs ===

@Schema(description = "Artist channel response")
data class ArtistChannelResponse(
    @Schema(description = "Channel ID")
    val id: UUID,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Streaming platform", example = "YOUTUBE")
    val platform: String,

    @Schema(description = "Channel handle (e.g., @NewJeans_official)")
    val channelHandle: String,

    @Schema(description = "Platform-specific channel ID")
    val channelId: String?,

    @Schema(description = "Channel URL")
    val channelUrl: String?,

    @Schema(description = "Whether this is an official channel")
    val isOfficial: Boolean,

    @Schema(description = "Whether crawling is active")
    val isActive: Boolean,

    @Schema(description = "Last crawled timestamp")
    val lastCrawledAt: Instant?,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant
) {
    companion object {
        fun from(channel: ArtistChannel): ArtistChannelResponse = ArtistChannelResponse(
            id = channel.id,
            artistId = channel.artistId,
            platform = channel.platform.name,
            channelHandle = channel.channelHandle,
            channelId = channel.channelId,
            channelUrl = channel.channelUrl,
            isOfficial = channel.isOfficial,
            isActive = channel.isActive,
            lastCrawledAt = channel.lastCrawledAt,
            createdAt = channel.createdAt
        )
    }
}

@Schema(description = "List of artist channels")
data class ArtistChannelListResponse(
    @Schema(description = "List of channels")
    val content: List<ArtistChannelResponse>,

    @Schema(description = "Total number of channels")
    val totalElements: Long
)

// === Request DTOs (Admin) ===

@Schema(description = "Create artist channel request")
data class CreateArtistChannelRequest(
    @field:NotNull(message = "Artist ID is required")
    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Streaming platform", example = "YOUTUBE")
    val platform: StreamingPlatform = StreamingPlatform.YOUTUBE,

    @field:NotBlank(message = "Channel handle is required")
    @Schema(description = "Channel handle (e.g., @NewJeans_official)", example = "@IVEstarship")
    val channelHandle: String,

    @Schema(description = "Platform-specific channel ID")
    val channelId: String? = null,

    @Schema(description = "Channel URL")
    val channelUrl: String? = null,

    @Schema(description = "Whether this is an official channel", example = "true")
    val isOfficial: Boolean = true,

    @Schema(description = "Whether crawling is active", example = "true")
    val isActive: Boolean = true
)

@Schema(description = "Update artist channel request")
data class UpdateArtistChannelRequest(
    @Schema(description = "Channel handle")
    val channelHandle: String? = null,

    @Schema(description = "Platform-specific channel ID")
    val channelId: String? = null,

    @Schema(description = "Channel URL")
    val channelUrl: String? = null,

    @Schema(description = "Whether this is an official channel")
    val isOfficial: Boolean? = null,

    @Schema(description = "Whether crawling is active")
    val isActive: Boolean? = null
)
