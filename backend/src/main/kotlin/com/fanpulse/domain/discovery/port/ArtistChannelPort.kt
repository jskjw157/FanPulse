package com.fanpulse.domain.discovery.port

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.streaming.StreamingPlatform
import java.util.UUID

/**
 * Domain Port for ArtistChannel persistence.
 * Manages artist channel registration for live stream discovery.
 */
interface ArtistChannelPort {

    /**
     * Finds active channels by platform.
     */
    fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannel>

    /**
     * Finds channels by artist ID.
     */
    fun findByArtistId(artistId: UUID): List<ArtistChannel>

    /**
     * Saves a channel (create or update).
     */
    fun save(channel: ArtistChannel): ArtistChannel

    /**
     * Batch save support.
     */
    fun <S : ArtistChannel> saveAll(entities: Iterable<S>): List<S>

    /**
     * Finds all channels.
     */
    fun findAll(): List<ArtistChannel>

    /**
     * Finds a channel by ID.
     */
    fun findById(id: UUID): ArtistChannel?

    /**
     * Checks if a channel exists by ID.
     */
    fun existsById(id: UUID): Boolean

    /**
     * Deletes a channel by ID.
     */
    fun deleteById(id: UUID)

    /**
     * Deletes all channels.
     */
    fun deleteAll()

    /**
     * Finds a channel by platform and handle.
     */
    fun findByPlatformAndChannelHandle(platform: StreamingPlatform, channelHandle: String): ArtistChannel?
}
