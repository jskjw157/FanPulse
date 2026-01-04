package com.fanpulse.domain.streaming

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "streaming_events")
data class StreamingEvent(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 255)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "stream_url", columnDefinition = "TEXT", nullable = false)
    val streamUrl: String,

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    var thumbnailUrl: String? = null,

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    @Column(name = "scheduled_at", nullable = false)
    val scheduledAt: Instant,

    @Column(name = "started_at")
    var startedAt: Instant? = null,

    @Column(name = "ended_at")
    var endedAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var status: StreamingStatus = StreamingStatus.SCHEDULED,

    @Column(name = "viewer_count")
    var viewerCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    fun updateMetadata(title: String, thumbnailUrl: String?) {
        this.title = title
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl
        }
    }
}

enum class StreamingStatus {
    SCHEDULED,
    LIVE,
    ENDED
}
