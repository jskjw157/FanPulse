package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * JPA Repository for StreamingEventEntity
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에 위치
 * - JPA Entity (StreamingEventEntity)와 함께 동작
 * - Domain 모델을 직접 다루지 않음
 */
@Repository
interface StreamingEventJpaRepository : JpaRepository<StreamingEventEntity, UUID> {

    fun findByStatus(status: StreamingStatus): List<StreamingEventEntity>

    fun findByStatusNot(status: StreamingStatus): List<StreamingEventEntity>

    fun findByPlatformAndExternalId(platform: StreamingPlatform, externalId: String): StreamingEventEntity?

    fun findByStreamUrl(streamUrl: String): StreamingEventEntity?
}
