package com.fanpulse.domain.discovery

import com.fanpulse.domain.discovery.port.ArtistChannelPort
import com.fanpulse.domain.streaming.StreamingPlatform
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ArtistChannelRepository : JpaRepository<ArtistChannel, UUID>, ArtistChannelPort {
    override fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannel>
    override fun findByArtistId(artistId: UUID): List<ArtistChannel>
}
