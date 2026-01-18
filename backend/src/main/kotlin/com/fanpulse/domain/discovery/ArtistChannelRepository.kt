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

    /**
     * Finds a channel by platform and handle (for duplicate check).
     */
    fun findByPlatformAndChannelHandle(platform: StreamingPlatform, channelHandle: String): ArtistChannel?

    /** P0-3: JpaRepository.saveAll을 Port 인터페이스에 맞게 위임 */
    @Suppress("UNCHECKED_CAST")
    override fun <S : ArtistChannel> saveAll(entities: Iterable<S>): List<S>
}
