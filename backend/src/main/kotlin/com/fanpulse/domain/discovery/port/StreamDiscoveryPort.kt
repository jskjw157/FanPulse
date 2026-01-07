package com.fanpulse.domain.discovery.port

import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import java.time.Instant

data class DiscoveredStream(
    val platform: StreamingPlatform,
    val externalId: String,
    val title: String,
    val description: String? = null,
    val streamUrl: String,
    val sourceUrl: String? = null,
    val thumbnailUrl: String? = null,
    val scheduledAt: Instant? = null,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val status: StreamingStatus = StreamingStatus.ENDED,
    val viewerCount: Int? = null
)

interface StreamDiscoveryPort {
    fun discoverChannelStreams(channelHandle: String): List<DiscoveredStream>
}
