package com.fanpulse.infrastructure.persistence.discovery

import com.fanpulse.domain.discovery.ArtistChannel

/**
 * ArtistChannel Domain ↔ Entity 변환 Mapper
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에서 Domain과 Entity 간 변환을 담당
 * - Domain 모델은 JPA에 의존하지 않음
 * - 양방향 변환을 지원하여 영속성 계층 격리
 */
object ArtistChannelMapper {

    /**
     * JPA Entity를 Domain 모델로 변환합니다.
     *
     * @param entity JPA 엔티티
     * @return Domain 모델
     */
    fun toDomain(entity: ArtistChannelEntity): ArtistChannel {
        return ArtistChannel.reconstitute(
            id = entity.id,
            artistId = entity.artistId,
            platform = entity.platform,
            channelHandle = entity.channelHandle,
            channelId = entity.channelId,
            channelUrl = entity.channelUrl,
            isOfficial = entity.isOfficial,
            isActive = entity.isActive,
            lastCrawledAt = entity.lastCrawledAt,
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain 모델을 JPA Entity로 변환합니다.
     *
     * @param domain Domain 모델
     * @return JPA 엔티티
     */
    fun toEntity(domain: ArtistChannel): ArtistChannelEntity {
        return ArtistChannelEntity(
            id = domain.id,
            artistId = domain.artistId,
            platform = domain.platform,
            channelHandle = domain.channelHandle,
            channelId = domain.channelId,
            channelUrl = domain.channelUrl,
            isOfficial = domain.isOfficial,
            isActive = domain.isActive,
            lastCrawledAt = domain.lastCrawledAt,
            createdAt = domain.createdAt
        )
    }
}
