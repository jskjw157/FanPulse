package com.fanpulse.domain.discovery

import com.fanpulse.domain.streaming.StreamingPlatform
import java.time.Instant
import java.util.UUID

/**
 * 아티스트 채널 도메인 모델
 *
 * K-Pop 아티스트의 스트리밍 플랫폼 채널 정보를 나타냅니다.
 * 이 클래스는 순수한 도메인 로직만 포함하며, JPA 어노테이션이 없습니다.
 *
 * Clean Architecture 원칙:
 * - 도메인 계층은 외부 프레임워크(JPA)에 의존하지 않습니다
 * - 비즈니스 로직은 도메인 모델에 캡슐화됩니다
 * - 영속성은 Infrastructure 계층의 Entity와 Mapper를 통해 처리됩니다
 */
class ArtistChannel private constructor(
    val id: UUID,
    val artistId: UUID,
    platform: StreamingPlatform,
    channelHandle: String,
    channelId: String?,
    channelUrl: String?,
    isOfficial: Boolean,
    isActive: Boolean,
    lastCrawledAt: Instant?,
    val createdAt: Instant
) {
    var platform: StreamingPlatform = platform
        private set

    var channelHandle: String = channelHandle
        private set

    var channelId: String? = channelId
        private set

    var channelUrl: String? = channelUrl
        private set

    var isOfficial: Boolean = isOfficial
        private set

    var isActive: Boolean = isActive
        private set

    var lastCrawledAt: Instant? = lastCrawledAt
        private set

    companion object {
        /**
         * 새로운 아티스트 채널을 생성합니다.
         */
        fun create(
            id: UUID = UUID.randomUUID(),
            artistId: UUID,
            platform: StreamingPlatform = StreamingPlatform.YOUTUBE,
            channelHandle: String,
            channelId: String? = null,
            channelUrl: String? = null,
            isOfficial: Boolean = true,
            isActive: Boolean = true,
            lastCrawledAt: Instant? = null,
            createdAt: Instant = Instant.now()
        ): ArtistChannel {
            return ArtistChannel(
                id = id,
                artistId = artistId,
                platform = platform,
                channelHandle = channelHandle,
                channelId = channelId,
                channelUrl = channelUrl,
                isOfficial = isOfficial,
                isActive = isActive,
                lastCrawledAt = lastCrawledAt,
                createdAt = createdAt
            )
        }

        /**
         * 영속성 계층에서 복원할 때 사용합니다.
         * Mapper에서만 사용해야 합니다.
         */
        fun reconstitute(
            id: UUID,
            artistId: UUID,
            platform: StreamingPlatform,
            channelHandle: String,
            channelId: String?,
            channelUrl: String?,
            isOfficial: Boolean,
            isActive: Boolean,
            lastCrawledAt: Instant?,
            createdAt: Instant
        ): ArtistChannel {
            return ArtistChannel(
                id = id,
                artistId = artistId,
                platform = platform,
                channelHandle = channelHandle,
                channelId = channelId,
                channelUrl = channelUrl,
                isOfficial = isOfficial,
                isActive = isActive,
                lastCrawledAt = lastCrawledAt,
                createdAt = createdAt
            )
        }
    }

    /**
     * 크롤링 완료 시간을 기록합니다.
     */
    fun markCrawled(now: Instant = Instant.now()) {
        lastCrawledAt = now
    }

    /**
     * 채널을 비활성화합니다.
     */
    fun deactivate() {
        isActive = false
    }

    /**
     * 채널을 활성화합니다.
     */
    fun activate() {
        isActive = true
    }

    /**
     * 채널 정보를 업데이트합니다.
     */
    fun updateChannelInfo(newChannelId: String?, newChannelUrl: String?) {
        if (!newChannelId.isNullOrBlank()) {
            channelId = newChannelId
        }
        if (!newChannelUrl.isNullOrBlank()) {
            channelUrl = newChannelUrl
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ArtistChannel
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ArtistChannel(id=$id, platform=$platform, channelHandle='$channelHandle', isActive=$isActive)"
    }
}
