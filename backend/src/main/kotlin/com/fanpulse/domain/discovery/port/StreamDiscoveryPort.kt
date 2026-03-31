package com.fanpulse.domain.discovery.port

import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import java.time.Instant

/**
 * 외부 플랫폼에서 탐색된 라이브 스트리밍 정보를 담는 DTO.
 *
 * @property platform streaming platform
 * @property externalId platform-specific video/stream ID
 * @property title stream title
 * @property streamUrl embeddable stream URL
 * @property status discovered stream status
 */
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

/**
 * 외부 플랫폼(YouTube 등)에서 라이브 스트리밍을 탐색하는 도메인 포트.
 */
interface StreamDiscoveryPort {
    /** Discovers live/upcoming streams from the given channel handle. */
    fun discoverChannelStreams(channelHandle: String): List<DiscoveredStream>
}
