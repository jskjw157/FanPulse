package com.fanpulse.domain.streaming

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "streaming_events")
class StreamingEvent(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    title: String,

    description: String? = null,

    platform: StreamingPlatform? = null,

    externalId: String? = null,

    @Column(name = "stream_url", columnDefinition = "TEXT", nullable = false)
    val streamUrl: String,

    sourceUrl: String? = null,

    thumbnailUrl: String? = null,

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    scheduledAt: Instant,

    startedAt: Instant? = null,

    endedAt: Instant? = null,

    status: StreamingStatus = StreamingStatus.SCHEDULED,

    viewerCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    @Column(nullable = false, length = 255)
    var title: String = title
        private set

    @Column(columnDefinition = "TEXT")
    var description: String? = description
        private set

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var platform: StreamingPlatform? = platform
        private set

    @Column(name = "external_id", length = 100)
    var externalId: String? = externalId
        private set

    @Column(name = "source_url", columnDefinition = "TEXT")
    var sourceUrl: String? = sourceUrl
        private set

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    var thumbnailUrl: String? = thumbnailUrl
        private set

    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: Instant = scheduledAt
        private set

    @Column(name = "started_at")
    var startedAt: Instant? = startedAt
        private set

    @Column(name = "ended_at")
    var endedAt: Instant? = endedAt
        private set

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var status: StreamingStatus = status
        private set

    @Column(name = "viewer_count")
    var viewerCount: Int = viewerCount
        private set

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
}

enum class StreamingStatus {
    SCHEDULED,
    LIVE,
    ENDED
}
