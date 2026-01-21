package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.streaming.StreamingEvent

/**
 * StreamingEvent Domain ↔ Entity 변환 Mapper
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에서 Domain과 Entity 간 변환을 담당
 * - Domain 모델은 JPA에 의존하지 않음
 * - 양방향 변환을 지원하여 영속성 계층 격리
 */
object StreamingEventMapper {

    /**
     * JPA Entity를 Domain 모델로 변환합니다.
     *
     * @param entity JPA 엔티티
     * @return Domain 모델
     */
    fun toDomain(entity: StreamingEventEntity): StreamingEvent {
        return StreamingEvent.reconstitute(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            platform = entity.platform,
            externalId = entity.externalId,
            streamUrl = entity.streamUrl,
            sourceUrl = entity.sourceUrl,
            thumbnailUrl = entity.thumbnailUrl,
            artistId = entity.artistId,
            scheduledAt = entity.scheduledAt,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            status = entity.status,
            viewerCount = entity.viewerCount,
            createdAt = entity.createdAt
        )
    }

    /**
     * Domain 모델을 JPA Entity로 변환합니다.
     *
     * @param domain Domain 모델
     * @return JPA 엔티티
     */
    fun toEntity(domain: StreamingEvent): StreamingEventEntity {
        return StreamingEventEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            platform = domain.platform,
            externalId = domain.externalId,
            streamUrl = domain.streamUrl,
            sourceUrl = domain.sourceUrl,
            thumbnailUrl = domain.thumbnailUrl,
            artistId = domain.artistId,
            scheduledAt = domain.scheduledAt,
            startedAt = domain.startedAt,
            endedAt = domain.endedAt,
            status = domain.status,
            viewerCount = domain.viewerCount,
            createdAt = domain.createdAt
        )
    }
}
