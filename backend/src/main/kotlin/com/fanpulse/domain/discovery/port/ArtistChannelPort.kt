package com.fanpulse.domain.discovery.port

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.streaming.StreamingPlatform
import java.util.Optional
import java.util.UUID

/**
 * Domain Port for ArtistChannel persistence.
 * This interface defines the contract for persistence operations
 * without any infrastructure dependencies.
 */
interface ArtistChannelPort {
    fun findById(id: UUID): Optional<ArtistChannel>
    fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannel>
    fun findByArtistId(artistId: UUID): List<ArtistChannel>
    fun save(channel: ArtistChannel): ArtistChannel

    /** P0-3: Batch save 지원 */
    fun saveAll(channels: Iterable<ArtistChannel>): List<ArtistChannel>

    fun deleteAll()
}
