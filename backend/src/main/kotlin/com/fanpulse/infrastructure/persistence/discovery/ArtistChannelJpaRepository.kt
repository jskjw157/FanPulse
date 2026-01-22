package com.fanpulse.infrastructure.persistence.discovery

import com.fanpulse.domain.streaming.StreamingPlatform
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * JPA Repository for ArtistChannelEntity
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에 위치
 * - JPA Entity (ArtistChannelEntity)와 함께 동작
 * - Domain 모델을 직접 다루지 않음
 */
@Repository
interface ArtistChannelJpaRepository : JpaRepository<ArtistChannelEntity, UUID> {

    fun findByPlatformAndIsActiveTrue(platform: StreamingPlatform): List<ArtistChannelEntity>

    fun findByArtistId(artistId: UUID): List<ArtistChannelEntity>
}
