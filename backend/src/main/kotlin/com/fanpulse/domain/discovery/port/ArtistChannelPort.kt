package com.fanpulse.domain.discovery.port

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.streaming.StreamingPlatform
import java.util.UUID

interface ArtistChannelPort {
    fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannel>
    fun findByArtistId(artistId: UUID): List<ArtistChannel>
    fun save(channel: ArtistChannel): ArtistChannel
}
