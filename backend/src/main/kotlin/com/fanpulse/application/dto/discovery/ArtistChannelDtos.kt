package com.fanpulse.application.dto.discovery

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.streaming.StreamingPlatform
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

// === Response DTOs ===

/**
 * 아티스트 채널 상세 정보 응답 모델.
 */
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

/**
 * 아티스트 채널 목록 응답 모델.
 */
@Schema(description = "List of artist channels")
data class ArtistChannelListResponse(
    @Schema(description = "List of channels")
    val content: List<ArtistChannelResponse>,

    @Schema(description = "Total number of channels")
    val totalElements: Long
)

// === Request DTOs (Admin) ===

/**
 * 관리자가 새 아티스트 채널을 크롤링 대상으로 등록하는 요청 모델.
 */
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

/**
 * 기존 아티스트 채널 정보를 부분 수정하는 요청 모델. null 필드는 변경하지 않는다.
 */
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
