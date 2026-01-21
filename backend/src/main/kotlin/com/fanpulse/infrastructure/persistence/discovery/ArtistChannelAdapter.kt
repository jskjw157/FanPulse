package com.fanpulse.infrastructure.persistence.discovery

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.discovery.port.ArtistChannelPort
import com.fanpulse.domain.streaming.StreamingPlatform
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

/**
 * ArtistChannelPort 구현체 (Adapter)
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에 위치
 * - Domain Port를 구현하여 Application 계층에 주입
 * - JpaRepository와 Mapper를 사용하여 Domain ↔ Entity 변환
 */
@Component
class ArtistChannelAdapter(
    private val jpaRepository: ArtistChannelJpaRepository
) : ArtistChannelPort {

    override fun findById(id: UUID): Optional<ArtistChannel> {
        return jpaRepository.findById(id).map { entity ->
            ArtistChannelMapper.toDomain(entity)
        }
    }

    override fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannel> {
        return jpaRepository.findByPlatformAndIsActiveTrue(platform).map { entity ->
            ArtistChannelMapper.toDomain(entity)
        }
    }

    override fun findByArtistId(artistId: UUID): List<ArtistChannel> {
        return jpaRepository.findByArtistId(artistId).map { entity ->
            ArtistChannelMapper.toDomain(entity)
        }
    }

    override fun save(channel: ArtistChannel): ArtistChannel {
        val entity = ArtistChannelMapper.toEntity(channel)
        val savedEntity = jpaRepository.save(entity)
        return ArtistChannelMapper.toDomain(savedEntity)
    }

    override fun saveAll(channels: Iterable<ArtistChannel>): List<ArtistChannel> {
        val entities = channels.map { ArtistChannelMapper.toEntity(it) }
        return jpaRepository.saveAll(entities).map { ArtistChannelMapper.toDomain(it) }
    }

    override fun deleteAll() {
        jpaRepository.deleteAll()
    }
}
