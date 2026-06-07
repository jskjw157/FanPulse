package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.ArtistChannelListResponse as DataArtistChannelListResponse
import com.aos.fanpulse.data.remote.dto.ArtistChannel as DataArtistChannel
import com.aos.fanpulse.data.remote.dto.ArtistChannelRequest as DataArtistChannelRequest
import com.aos.fanpulse.data.remote.dto.ChannelDiscoverResponse as DataChannelDiscoverResponse

// [Domain 계층 Model] (앱 내부 비즈니스 로직용)
import com.aos.fanpulse.domain.model.ArtistChannelListResponse as DomainArtistChannelListResponse
import com.aos.fanpulse.domain.model.ArtistChannel as DomainArtistChannel
import com.aos.fanpulse.domain.model.ArtistChannelRequest as DomainArtistChannelRequest
import com.aos.fanpulse.domain.model.ChannelDiscoverResponse as DomainChannelDiscoverResponse

internal fun DataArtistChannel.toDomain(): DomainArtistChannel {
    return DomainArtistChannel(
        id = this.id,
        artistId = this.artistId,
        platform = this.platform,
        channelHandle = this.channelHandle ?: "",
        channelId = this.channelId ?: "",
        channelUrl = this.channelUrl ?: "",
        isOfficial = this.isOfficial,
        isActive = this.isActive,
        lastCrawledAt = this.lastCrawledAt,
        createdAt = this.createdAt
    )
}

internal fun DataArtistChannelListResponse.toDomain(): DomainArtistChannelListResponse {
    return DomainArtistChannelListResponse(
        content = this.content.map { it.toDomain() },
        totalElements = this.totalElements
    )
}

internal fun DataChannelDiscoverResponse.toDomain(): DomainChannelDiscoverResponse {
    return DomainChannelDiscoverResponse(
        total = this.total,
        upserted = this.upserted,
        failed = this.failed,
        errors = this.errors
    )
}

internal fun DomainArtistChannelRequest.toData(): DataArtistChannelRequest {
    return DataArtistChannelRequest(
        artistId = this.artistId,
        platform = this.platform,
        channelHandle = this.channelHandle,
        channelId = this.channelId,
        channelUrl = this.channelUrl,
        isOfficial = this.isOfficial,
        isActive = this.isActive
    )
}