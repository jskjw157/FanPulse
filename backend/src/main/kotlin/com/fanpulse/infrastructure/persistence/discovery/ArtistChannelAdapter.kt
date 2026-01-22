package com.fanpulse.infrastructure.persistence.discovery

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.discovery.port.ArtistChannelPort
import com.fanpulse.domain.streaming.StreamingPlatform
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Adapter that implements ArtistChannelPort using Spring Data JPA Repository.
 * Follows Hexagonal Architecture (Ports & Adapters) pattern.
 */
@Component
class ArtistChannelAdapter(
    private val repository: ArtistChannelJpaRepository
) : ArtistChannelPort {

    override fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannel> {
        return repository.findByPlatformAndIsActiveTrue(platform)
    }

    override fun findByArtistId(artistId: UUID): List<ArtistChannel> {
        return repository.findByArtistId(artistId)
    }

    override fun save(channel: ArtistChannel): ArtistChannel {
        return repository.save(channel)
    }

    override fun <S : ArtistChannel> saveAll(entities: Iterable<S>): List<S> {
        return repository.saveAll(entities)
    }

    override fun findAll(): List<ArtistChannel> {
        return repository.findAll()
    }

    override fun findById(id: UUID): ArtistChannel? {
        return repository.findById(id).orElse(null)
    }

    override fun existsById(id: UUID): Boolean {
        return repository.existsById(id)
    }

    override fun deleteById(id: UUID) {
        repository.deleteById(id)
    }

    override fun deleteAll() {
        repository.deleteAll()
    }

    override fun findByPlatformAndChannelHandle(platform: StreamingPlatform, channelHandle: String): ArtistChannel? {
        return repository.findByPlatformAndChannelHandle(platform, channelHandle)
    }
}
