package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

/**
 * StreamingEventPort 구현체 (Adapter)
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에 위치
 * - Domain Port를 구현하여 Application 계층에 주입
 * - JpaRepository와 Mapper를 사용하여 Domain ↔ Entity 변환
 */
@Component
class StreamingEventAdapter(
    private val jpaRepository: StreamingEventJpaRepository
) : StreamingEventPort {

    override fun findEventById(id: UUID): StreamingEvent? {
        return jpaRepository.findById(id).orElse(null)?.let { entity ->
            StreamingEventMapper.toDomain(entity)
        }
    }

    override fun findByStatus(status: StreamingStatus): List<StreamingEvent> {
        return jpaRepository.findByStatus(status).map { entity ->
            StreamingEventMapper.toDomain(entity)
        }
    }

    override fun findByStatusNot(status: StreamingStatus): List<StreamingEvent> {
        return jpaRepository.findByStatusNot(status).map { entity ->
            StreamingEventMapper.toDomain(entity)
        }
    }

    override fun findByPlatformAndExternalId(platform: StreamingPlatform, externalId: String): StreamingEvent? {
        return jpaRepository.findByPlatformAndExternalId(platform, externalId)?.let { entity ->
            StreamingEventMapper.toDomain(entity)
        }
    }

    override fun findByStreamUrl(streamUrl: String): StreamingEvent? {
        return jpaRepository.findByStreamUrl(streamUrl)?.let { entity ->
            StreamingEventMapper.toDomain(entity)
        }
    }

    override fun findById(id: UUID): Optional<StreamingEvent> {
        return jpaRepository.findById(id).map { entity ->
            StreamingEventMapper.toDomain(entity)
        }
    }

    override fun save(event: StreamingEvent): StreamingEvent {
        val entity = StreamingEventMapper.toEntity(event)
        val savedEntity = jpaRepository.save(entity)
        return StreamingEventMapper.toDomain(savedEntity)
    }

    override fun saveAll(events: Iterable<StreamingEvent>): List<StreamingEvent> {
        val entities = events.map { StreamingEventMapper.toEntity(it) }
        return jpaRepository.saveAll(entities).map { StreamingEventMapper.toDomain(it) }
    }

    override fun deleteAll() {
        jpaRepository.deleteAll()
    }
}
