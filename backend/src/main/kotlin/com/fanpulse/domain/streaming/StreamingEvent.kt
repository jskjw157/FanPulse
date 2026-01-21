package com.fanpulse.domain.streaming

import java.time.Instant
import java.util.*

/**
 * 스트리밍 이벤트 도메인 모델
 *
 * K-Pop 아티스트의 라이브 스트리밍 이벤트를 나타냅니다.
 * 이 클래스는 순수한 도메인 로직만 포함하며, JPA 어노테이션이 없습니다.
 *
 * Clean Architecture 원칙:
 * - 도메인 계층은 외부 프레임워크(JPA)에 의존하지 않습니다
 * - 비즈니스 로직은 도메인 모델에 캡슐화됩니다
 * - 영속성은 Infrastructure 계층의 Entity와 Mapper를 통해 처리됩니다
 */
class StreamingEvent private constructor(
    val id: UUID,
    title: String,
    description: String?,
    platform: StreamingPlatform?,
    externalId: String?,
    val streamUrl: String,
    sourceUrl: String?,
    thumbnailUrl: String?,
    val artistId: UUID,
    scheduledAt: Instant,
    startedAt: Instant?,
    endedAt: Instant?,
    status: StreamingStatus,
    viewerCount: Int,
    val createdAt: Instant
) {
    var title: String = title
        private set

    var description: String? = description
        private set

    var platform: StreamingPlatform? = platform
        private set

    var externalId: String? = externalId
        private set

    var sourceUrl: String? = sourceUrl
        private set

    var thumbnailUrl: String? = thumbnailUrl
        private set

    var scheduledAt: Instant = scheduledAt
        private set

    var startedAt: Instant? = startedAt
        private set

    var endedAt: Instant? = endedAt
        private set

    var status: StreamingStatus = status
        private set

    var viewerCount: Int = viewerCount
        private set

    companion object {
        /**
         * 새로운 스트리밍 이벤트를 생성합니다.
         */
        fun create(
            id: UUID = UUID.randomUUID(),
            title: String,
            description: String? = null,
            platform: StreamingPlatform? = null,
            externalId: String? = null,
            streamUrl: String,
            sourceUrl: String? = null,
            thumbnailUrl: String? = null,
            artistId: UUID,
            scheduledAt: Instant,
            startedAt: Instant? = null,
            endedAt: Instant? = null,
            status: StreamingStatus = StreamingStatus.SCHEDULED,
            viewerCount: Int = 0,
            createdAt: Instant = Instant.now()
        ): StreamingEvent {
            return StreamingEvent(
                id = id,
                title = title,
                description = description,
                platform = platform,
                externalId = externalId,
                streamUrl = streamUrl,
                sourceUrl = sourceUrl,
                thumbnailUrl = thumbnailUrl,
                artistId = artistId,
                scheduledAt = scheduledAt,
                startedAt = startedAt,
                endedAt = endedAt,
                status = status,
                viewerCount = viewerCount,
                createdAt = createdAt
            )
        }

        /**
         * 영속성 계층에서 복원할 때 사용합니다.
         * Mapper에서만 사용해야 합니다.
         */
        fun reconstitute(
            id: UUID,
            title: String,
            description: String?,
            platform: StreamingPlatform?,
            externalId: String?,
            streamUrl: String,
            sourceUrl: String?,
            thumbnailUrl: String?,
            artistId: UUID,
            scheduledAt: Instant,
            startedAt: Instant?,
            endedAt: Instant?,
            status: StreamingStatus,
            viewerCount: Int,
            createdAt: Instant
        ): StreamingEvent {
            return StreamingEvent(
                id = id,
                title = title,
                description = description,
                platform = platform,
                externalId = externalId,
                streamUrl = streamUrl,
                sourceUrl = sourceUrl,
                thumbnailUrl = thumbnailUrl,
                artistId = artistId,
                scheduledAt = scheduledAt,
                startedAt = startedAt,
                endedAt = endedAt,
                status = status,
                viewerCount = viewerCount,
                createdAt = createdAt
            )
        }
    }

    /**
     * Updates metadata from external source (e.g., YouTube oEmbed).
     */
    fun updateMetadata(newTitle: String, newThumbnailUrl: String?) {
        this.title = newTitle
        if (newThumbnailUrl != null) {
            this.thumbnailUrl = newThumbnailUrl
        }
    }

    /**
     * Updates source identity fields when discovery data provides them.
     */
    fun updateSourceIdentity(newPlatform: StreamingPlatform?, newExternalId: String?) {
        if (newPlatform != null) {
            platform = newPlatform
        }
        if (!newExternalId.isNullOrBlank()) {
            externalId = newExternalId
        }
    }

    /**
     * Updates the canonical source URL.
     */
    fun updateSourceUrl(newSourceUrl: String?) {
        if (!newSourceUrl.isNullOrBlank()) {
            sourceUrl = newSourceUrl
        }
    }

    /**
     * Applies status and timing updates from discovery data.
     */
    fun applyDiscoveryStatus(
        newStatus: StreamingStatus,
        discoveredScheduledAt: Instant?,
        discoveredStartedAt: Instant?,
        discoveredEndedAt: Instant?
    ) {
        if (discoveredScheduledAt != null) {
            scheduledAt = discoveredScheduledAt
        }

        when (newStatus) {
            StreamingStatus.SCHEDULED -> {
                if (status == StreamingStatus.SCHEDULED) {
                    if (discoveredStartedAt != null) {
                        startedAt = discoveredStartedAt
                    }
                    if (discoveredEndedAt != null) {
                        endedAt = discoveredEndedAt
                    }
                }
            }
            StreamingStatus.LIVE -> {
                if (status == StreamingStatus.SCHEDULED) {
                    status = StreamingStatus.LIVE
                }
                if (discoveredStartedAt != null) {
                    startedAt = discoveredStartedAt
                }
            }
            StreamingStatus.ENDED -> {
                if (status != StreamingStatus.ENDED) {
                    status = StreamingStatus.ENDED
                }
                if (discoveredStartedAt != null) {
                    startedAt = discoveredStartedAt
                }
                if (discoveredEndedAt != null) {
                    endedAt = discoveredEndedAt
                }
            }
        }
    }

    /**
     * Transitions the event to LIVE status.
     * @throws IllegalStateException if current status is not SCHEDULED
     */
    fun goLive(now: Instant = Instant.now()) {
        require(status == StreamingStatus.SCHEDULED) {
            "Cannot go live from status: $status. Only SCHEDULED events can go live."
        }
        status = StreamingStatus.LIVE
        startedAt = now
    }

    /**
     * Ends the streaming event.
     * @throws IllegalStateException if current status is not LIVE
     */
    fun end(now: Instant = Instant.now()) {
        require(status == StreamingStatus.LIVE) {
            "Cannot end from status: $status. Only LIVE events can be ended."
        }
        status = StreamingStatus.ENDED
        endedAt = now
    }

    /**
     * Updates the viewer count.
     */
    fun updateViewerCount(count: Int) {
        require(count >= 0) { "Viewer count cannot be negative: $count" }
        this.viewerCount = count
    }

    /**
     * Updates the description.
     */
    fun updateDescription(newDescription: String?) {
        this.description = newDescription
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as StreamingEvent
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "StreamingEvent(id=$id, title='$title', status=$status, platform=$platform)"
    }
}

/**
 * 스트리밍 상태
 */
enum class StreamingStatus {
    SCHEDULED,
    LIVE,
    ENDED
}
