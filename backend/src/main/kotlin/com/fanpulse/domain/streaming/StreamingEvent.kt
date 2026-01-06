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

    @Column(name = "stream_url", columnDefinition = "TEXT", nullable = false)
    val streamUrl: String,

    thumbnailUrl: String? = null,

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    @Column(name = "scheduled_at", nullable = false)
    val scheduledAt: Instant,

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

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    var thumbnailUrl: String? = thumbnailUrl
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
