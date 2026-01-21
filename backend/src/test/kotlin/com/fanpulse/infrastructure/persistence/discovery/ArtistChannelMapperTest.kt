package com.fanpulse.infrastructure.persistence.discovery

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.streaming.StreamingPlatform
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * ArtistChannelMapper 테스트
 *
 * Domain ↔ Entity 변환이 올바르게 동작하는지 검증합니다.
 * TDD RED Phase: 이 테스트는 Mapper가 구현되기 전에 작성됩니다.
 */
@DisplayName("ArtistChannelMapper")
class ArtistChannelMapperTest {

    @Nested
    @DisplayName("toDomain")
    inner class ToDomain {

        @Test
        @DisplayName("should convert all fields from Entity to Domain")
        fun shouldConvertAllFields() {
            // Given
            val entity = ArtistChannelEntity(
                id = UUID.randomUUID(),
                artistId = UUID.randomUUID(),
                platform = StreamingPlatform.YOUTUBE,
                channelHandle = "@TestChannel",
                channelId = "UC123456789",
                channelUrl = "https://www.youtube.com/@TestChannel",
                isOfficial = true,
                isActive = true,
                lastCrawledAt = Instant.now(),
                createdAt = Instant.now()
            )

            // When
            val domain = ArtistChannelMapper.toDomain(entity)

            // Then
            assertAll(
                { assertEquals(entity.id, domain.id) },
                { assertEquals(entity.artistId, domain.artistId) },
                { assertEquals(entity.platform, domain.platform) },
                { assertEquals(entity.channelHandle, domain.channelHandle) },
                { assertEquals(entity.channelId, domain.channelId) },
                { assertEquals(entity.channelUrl, domain.channelUrl) },
                { assertEquals(entity.isOfficial, domain.isOfficial) },
                { assertEquals(entity.isActive, domain.isActive) },
                { assertEquals(entity.lastCrawledAt, domain.lastCrawledAt) },
                { assertEquals(entity.createdAt, domain.createdAt) }
            )
        }

        @Test
        @DisplayName("should handle nullable fields correctly")
        fun shouldHandleNullableFields() {
            // Given
            val entity = ArtistChannelEntity(
                id = UUID.randomUUID(),
                artistId = UUID.randomUUID(),
                platform = StreamingPlatform.YOUTUBE,
                channelHandle = "@TestChannel",
                channelId = null,
                channelUrl = null,
                isOfficial = false,
                isActive = true,
                lastCrawledAt = null,
                createdAt = Instant.now()
            )

            // When
            val domain = ArtistChannelMapper.toDomain(entity)

            // Then
            assertAll(
                { assertNull(domain.channelId) },
                { assertNull(domain.channelUrl) },
                { assertNull(domain.lastCrawledAt) },
                { assertFalse(domain.isOfficial) }
            )
        }

        @Test
        @DisplayName("should convert inactive channel correctly")
        fun shouldConvertInactiveChannel() {
            // Given
            val entity = ArtistChannelEntity(
                id = UUID.randomUUID(),
                artistId = UUID.randomUUID(),
                platform = StreamingPlatform.YOUTUBE,
                channelHandle = "@InactiveChannel",
                channelId = null,
                channelUrl = null,
                isOfficial = true,
                isActive = false,
                lastCrawledAt = Instant.now().minusSeconds(86400),
                createdAt = Instant.now()
            )

            // When
            val domain = ArtistChannelMapper.toDomain(entity)

            // Then
            assertFalse(domain.isActive)
        }
    }

    @Nested
    @DisplayName("toEntity")
    inner class ToEntity {

        @Test
        @DisplayName("should convert all fields from Domain to Entity")
        fun shouldConvertAllFields() {
            // Given
            val domain = ArtistChannel.create(
                id = UUID.randomUUID(),
                artistId = UUID.randomUUID(),
                platform = StreamingPlatform.YOUTUBE,
                channelHandle = "@TestChannel",
                channelId = "UC123456789",
                channelUrl = "https://www.youtube.com/@TestChannel",
                isOfficial = true,
                isActive = true,
                createdAt = Instant.now()
            )

            // When
            val entity = ArtistChannelMapper.toEntity(domain)

            // Then
            assertAll(
                { assertEquals(domain.id, entity.id) },
                { assertEquals(domain.artistId, entity.artistId) },
                { assertEquals(domain.platform, entity.platform) },
                { assertEquals(domain.channelHandle, entity.channelHandle) },
                { assertEquals(domain.channelId, entity.channelId) },
                { assertEquals(domain.channelUrl, entity.channelUrl) },
                { assertEquals(domain.isOfficial, entity.isOfficial) },
                { assertEquals(domain.isActive, entity.isActive) },
                { assertEquals(domain.lastCrawledAt, entity.lastCrawledAt) },
                { assertEquals(domain.createdAt, entity.createdAt) }
            )
        }

        @Test
        @DisplayName("should preserve domain state after round-trip conversion")
        fun shouldPreserveStateAfterRoundTrip() {
            // Given
            val originalId = UUID.randomUUID()
            val originalArtistId = UUID.randomUUID()
            val originalCreatedAt = Instant.now()

            val domain = ArtistChannel.create(
                id = originalId,
                artistId = originalArtistId,
                platform = StreamingPlatform.YOUTUBE,
                channelHandle = "@OriginalChannel",
                channelId = "UCoriginal123",
                channelUrl = "https://www.youtube.com/@OriginalChannel",
                isOfficial = true,
                isActive = true,
                createdAt = originalCreatedAt
            )

            // When: Round-trip conversion
            val entity = ArtistChannelMapper.toEntity(domain)
            val restored = ArtistChannelMapper.toDomain(entity)

            // Then: All fields should be preserved
            assertAll(
                { assertEquals(originalId, restored.id) },
                { assertEquals(originalArtistId, restored.artistId) },
                { assertEquals(StreamingPlatform.YOUTUBE, restored.platform) },
                { assertEquals("@OriginalChannel", restored.channelHandle) },
                { assertEquals("UCoriginal123", restored.channelId) },
                { assertEquals("https://www.youtube.com/@OriginalChannel", restored.channelUrl) },
                { assertTrue(restored.isOfficial) },
                { assertTrue(restored.isActive) },
                { assertEquals(originalCreatedAt, restored.createdAt) }
            )
        }
    }

    @Nested
    @DisplayName("Domain Business Logic Preservation")
    inner class BusinessLogicPreservation {

        @Test
        @DisplayName("should preserve markCrawled state after round-trip")
        fun shouldPreserveMarkCrawledState() {
            // Given
            val domain = ArtistChannel.create(
                artistId = UUID.randomUUID(),
                platform = StreamingPlatform.YOUTUBE,
                channelHandle = "@TestChannel"
            )

            val crawlTime = Instant.now()
            domain.markCrawled(crawlTime)

            // When: Round-trip conversion
            val entity = ArtistChannelMapper.toEntity(domain)
            val restored = ArtistChannelMapper.toDomain(entity)

            // Then
            assertEquals(crawlTime, restored.lastCrawledAt)
        }
    }
}
