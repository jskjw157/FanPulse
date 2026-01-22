package com.fanpulse.infrastructure.persistence.discovery

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.streaming.StreamingPlatform
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA Repository for ArtistChannel.
 * Used by ArtistChannelAdapter to implement the domain port.
 */
@Repository
interface ArtistChannelJpaRepository : JpaRepository<ArtistChannel, UUID> {

    fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannel>

    fun findByArtistId(artistId: UUID): List<ArtistChannel>

    /**
     * Finds a channel by platform and handle (for duplicate check).
     */
    fun findByPlatformAndChannelHandle(platform: StreamingPlatform, channelHandle: String): ArtistChannel?
}
