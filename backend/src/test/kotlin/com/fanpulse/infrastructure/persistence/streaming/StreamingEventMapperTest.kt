package com.fanpulse.infrastructure.persistence.streaming

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

/**
 * StreamingEventMapper 테스트
 *
 * Domain ↔ Entity 변환이 올바르게 동작하는지 검증합니다.
 * TDD RED Phase: 이 테스트는 Mapper가 구현되기 전에 작성됩니다.
 */
@DisplayName("StreamingEventMapper")
class StreamingEventMapperTest {

    @Nested
    @DisplayName("toDomain")
    inner class ToDomain {

        @Test
        @DisplayName("should convert all fields from Entity to Domain")
        fun shouldConvertAllFields() {
            // Given
            val entity = StreamingEventEntity(
                id = UUID.randomUUID(),
                title = "Test Stream",
                description = "Test Description",
                platform = StreamingPlatform.YOUTUBE,
                externalId = "abc123",
                streamUrl = "https://youtube.com/watch?v=abc123",
                sourceUrl = "https://youtube.com/live/abc123",
                thumbnailUrl = "https://img.youtube.com/vi/abc123/0.jpg",
                artistId = UUID.randomUUID(),
                scheduledAt = Instant.now().plusSeconds(3600),
                startedAt = null,
                endedAt = null,
                status = StreamingStatus.SCHEDULED,
                viewerCount = 0,
                createdAt = Instant.now()
            )

            // When
            val domain = StreamingEventMapper.toDomain(entity)

            // Then
            assertAll(
                { assertEquals(entity.id, domain.id) },
                { assertEquals(entity.title, domain.title) },
                { assertEquals(entity.description, domain.description) },
                { assertEquals(entity.platform, domain.platform) },
                { assertEquals(entity.externalId, domain.externalId) },
                { assertEquals(entity.streamUrl, domain.streamUrl) },
                { assertEquals(entity.sourceUrl, domain.sourceUrl) },
                { assertEquals(entity.thumbnailUrl, domain.thumbnailUrl) },
                { assertEquals(entity.artistId, domain.artistId) },
                { assertEquals(entity.scheduledAt, domain.scheduledAt) },
                { assertEquals(entity.startedAt, domain.startedAt) },
                { assertEquals(entity.endedAt, domain.endedAt) },
                { assertEquals(entity.status, domain.status) },
                { assertEquals(entity.viewerCount, domain.viewerCount) },
                { assertEquals(entity.createdAt, domain.createdAt) }
            )
        }

        @Test
        @DisplayName("should handle nullable fields correctly")
        fun shouldHandleNullableFields() {
            // Given
            val entity = StreamingEventEntity(
                id = UUID.randomUUID(),
                title = "Test Stream",
                description = null,
                platform = null,
                externalId = null,
                streamUrl = "https://youtube.com/watch?v=abc123",
                sourceUrl = null,
                thumbnailUrl = null,
                artistId = UUID.randomUUID(),
                scheduledAt = Instant.now(),
                startedAt = null,
                endedAt = null,
                status = StreamingStatus.SCHEDULED,
                viewerCount = 0,
                createdAt = Instant.now()
            )

            // When
            val domain = StreamingEventMapper.toDomain(entity)

            // Then
            assertAll(
                { assertNull(domain.description) },
                { assertNull(domain.platform) },
                { assertNull(domain.externalId) },
                { assertNull(domain.sourceUrl) },
                { assertNull(domain.thumbnailUrl) },
                { assertNull(domain.startedAt) },
                { assertNull(domain.endedAt) }
            )
        }

        @Test
        @DisplayName("should convert LIVE status with startedAt")
        fun shouldConvertLiveStatus() {
            // Given
            val startedAt = Instant.now()
            val entity = StreamingEventEntity(
                id = UUID.randomUUID(),
                title = "Live Stream",
                description = null,
                platform = StreamingPlatform.YOUTUBE,
                externalId = "live123",
                streamUrl = "https://youtube.com/watch?v=live123",
                sourceUrl = null,
                thumbnailUrl = null,
                artistId = UUID.randomUUID(),
                scheduledAt = startedAt.minusSeconds(60),
                startedAt = startedAt,
                endedAt = null,
                status = StreamingStatus.LIVE,
                viewerCount = 1500,
                createdAt = Instant.now()
            )

            // When
            val domain = StreamingEventMapper.toDomain(entity)

            // Then
            assertAll(
                { assertEquals(StreamingStatus.LIVE, domain.status) },
                { assertEquals(startedAt, domain.startedAt) },
                { assertEquals(1500, domain.viewerCount) }
            )
        }
    }

    @Nested
    @DisplayName("toEntity")
    inner class ToEntity {

        @Test
        @DisplayName("should convert all fields from Domain to Entity")
        fun shouldConvertAllFields() {
            // Given
            val domain = StreamingEvent.create(
                id = UUID.randomUUID(),
                title = "Test Stream",
                description = "Test Description",
                platform = StreamingPlatform.YOUTUBE,
                externalId = "abc123",
                streamUrl = "https://youtube.com/watch?v=abc123",
                sourceUrl = "https://youtube.com/live/abc123",
                thumbnailUrl = "https://img.youtube.com/vi/abc123/0.jpg",
                artistId = UUID.randomUUID(),
                scheduledAt = Instant.now().plusSeconds(3600),
                createdAt = Instant.now()
            )

            // When
            val entity = StreamingEventMapper.toEntity(domain)

            // Then
            assertAll(
                { assertEquals(domain.id, entity.id) },
                { assertEquals(domain.title, entity.title) },
                { assertEquals(domain.description, entity.description) },
                { assertEquals(domain.platform, entity.platform) },
                { assertEquals(domain.externalId, entity.externalId) },
                { assertEquals(domain.streamUrl, entity.streamUrl) },
                { assertEquals(domain.sourceUrl, entity.sourceUrl) },
                { assertEquals(domain.thumbnailUrl, entity.thumbnailUrl) },
                { assertEquals(domain.artistId, entity.artistId) },
                { assertEquals(domain.scheduledAt, entity.scheduledAt) },
                { assertEquals(domain.status, entity.status) },
                { assertEquals(domain.viewerCount, entity.viewerCount) },
                { assertEquals(domain.createdAt, entity.createdAt) }
            )
        }

        @Test
        @DisplayName("should preserve domain state after round-trip conversion")
        fun shouldPreserveStateAfterRoundTrip() {
            // Given
            val originalId = UUID.randomUUID()
            val originalArtistId = UUID.randomUUID()
            val originalScheduledAt = Instant.now().plusSeconds(3600)
            val originalCreatedAt = Instant.now()

            val domain = StreamingEvent.create(
                id = originalId,
                title = "Original Title",
                description = "Original Description",
                platform = StreamingPlatform.YOUTUBE,
                externalId = "orig123",
                streamUrl = "https://youtube.com/watch?v=orig123",
                sourceUrl = "https://youtube.com/live/orig123",
                thumbnailUrl = "https://img.youtube.com/vi/orig123/0.jpg",
                artistId = originalArtistId,
                scheduledAt = originalScheduledAt,
                createdAt = originalCreatedAt
            )

            // When: Round-trip conversion
            val entity = StreamingEventMapper.toEntity(domain)
            val restored = StreamingEventMapper.toDomain(entity)

            // Then: All fields should be preserved
            assertAll(
                { assertEquals(originalId, restored.id) },
                { assertEquals("Original Title", restored.title) },
                { assertEquals("Original Description", restored.description) },
                { assertEquals(StreamingPlatform.YOUTUBE, restored.platform) },
                { assertEquals("orig123", restored.externalId) },
                { assertEquals("https://youtube.com/watch?v=orig123", restored.streamUrl) },
                { assertEquals(originalArtistId, restored.artistId) },
                { assertEquals(originalScheduledAt, restored.scheduledAt) },
                { assertEquals(originalCreatedAt, restored.createdAt) },
                { assertEquals(StreamingStatus.SCHEDULED, restored.status) }
            )
        }
    }

    @Nested
    @DisplayName("Status Conversion")
    inner class StatusConversion {

        @Test
        @DisplayName("should convert all status values correctly")
        fun shouldConvertAllStatusValues() {
            val artistId = UUID.randomUUID()
            val now = Instant.now()

            for (status in StreamingStatus.values()) {
                // Given
                val entity = StreamingEventEntity(
                    id = UUID.randomUUID(),
                    title = "Test",
                    description = null,
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "test",
                    streamUrl = "https://test.com",
                    sourceUrl = null,
                    thumbnailUrl = null,
                    artistId = artistId,
                    scheduledAt = now,
                    startedAt = if (status != StreamingStatus.SCHEDULED) now else null,
                    endedAt = if (status == StreamingStatus.ENDED) now else null,
                    status = status,
                    viewerCount = 0,
                    createdAt = now
                )

                // When
                val domain = StreamingEventMapper.toDomain(entity)

                // Then
                assertEquals(status, domain.status, "Status $status should be preserved")
            }
        }
    }
}
