package com.fanpulse.domain.discovery

import com.fanpulse.domain.streaming.StreamingPlatform
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

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
class ArtistChannel(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var platform: StreamingPlatform = StreamingPlatform.YOUTUBE,

    @Column(name = "channel_handle", length = 100, nullable = false)
    var channelHandle: String,

    @Column(name = "channel_id", length = 100)
    var channelId: String? = null,

    @Column(name = "channel_url", columnDefinition = "TEXT")
    var channelUrl: String? = null,

    @Column(name = "is_official", nullable = false)
    var isOfficial: Boolean = true,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "last_crawled_at")
    var lastCrawledAt: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    fun markCrawled(now: Instant = Instant.now()) {
        lastCrawledAt = now
    }
}
