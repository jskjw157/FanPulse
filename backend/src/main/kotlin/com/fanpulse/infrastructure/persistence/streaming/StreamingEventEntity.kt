package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * StreamingEvent JPA 엔티티
 *
 * 데이터베이스 영속성을 위한 JPA 엔티티입니다.
 * 비즈니스 로직은 포함하지 않으며, 순수한 데이터 컨테이너 역할만 합니다.
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에 위치
 * - JPA 어노테이션은 이 클래스에만 존재
 * - Domain 모델과의 변환은 StreamingEventMapper를 통해 수행
 */
@Entity
@Table(name = "streaming_events")
data class StreamingEventEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 255)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    val platform: StreamingPlatform? = null,

    @Column(name = "external_id", length = 100)
    val externalId: String? = null,

    @Column(name = "stream_url", columnDefinition = "TEXT", nullable = false)
    val streamUrl: String,

    @Column(name = "source_url", columnDefinition = "TEXT")
    val sourceUrl: String? = null,

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    val thumbnailUrl: String? = null,

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    @Column(name = "scheduled_at", nullable = false)
    val scheduledAt: Instant,

    @Column(name = "started_at")
    val startedAt: Instant? = null,

    @Column(name = "ended_at")
    val endedAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val status: StreamingStatus = StreamingStatus.SCHEDULED,

    @Column(name = "viewer_count")
    val viewerCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
