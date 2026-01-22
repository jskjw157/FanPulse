package com.fanpulse.infrastructure.persistence.discovery

import com.fanpulse.domain.streaming.StreamingPlatform
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * ArtistChannel JPA 엔티티
 *
 * 데이터베이스 영속성을 위한 JPA 엔티티입니다.
 * 비즈니스 로직은 포함하지 않으며, 순수한 데이터 컨테이너 역할만 합니다.
 *
 * Clean Architecture 원칙:
 * - Infrastructure 계층에 위치
 * - JPA 어노테이션은 이 클래스에만 존재
 * - Domain 모델과의 변환은 ArtistChannelMapper를 통해 수행
 */
@Entity
@Table(
    name = "artist_channels",
    uniqueConstraints = [
        UniqueConstraint(
            name = "ux_artist_channels_platform_handle",
            columnNames = ["platform", "channel_handle"]
        )
    ]
)
data class ArtistChannelEntity(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val platform: StreamingPlatform = StreamingPlatform.YOUTUBE,

    @Column(name = "channel_handle", length = 100, nullable = false)
    val channelHandle: String,

    @Column(name = "channel_id", length = 100)
    val channelId: String? = null,

    @Column(name = "channel_url", columnDefinition = "TEXT")
    val channelUrl: String? = null,

    @Column(name = "is_official", nullable = false)
    val isOfficial: Boolean = true,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "last_crawled_at")
    val lastCrawledAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
