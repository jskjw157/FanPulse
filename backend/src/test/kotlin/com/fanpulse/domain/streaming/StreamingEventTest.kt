package com.fanpulse.domain.streaming

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import java.util.*

/**
 * StreamingEvent Domain Model Tests
 *
 * Tests for StreamingEvent entity including creation, status transitions, and property management.
 * These are pure unit tests without any external dependencies.
 */
@DisplayName("StreamingEvent Domain Model")
class StreamingEventTest {

    @Nested
    @DisplayName("StreamingEvent 생성")
    inner class StreamingEventCreationTests {

        @Test
        @DisplayName("유효한 데이터로 StreamingEvent 생성 성공")
        fun `should create StreamingEvent with valid data`() {
            // Given
            val id = UUID.randomUUID()
            val title = "BTS Live Concert"
            val artistId = UUID.randomUUID()
            val streamUrl = "https://youtube.com/watch?v=abc123"
            val scheduledAt = Instant.now().plusSeconds(3600)

            // When
            val event = StreamingEvent(
                id = id,
                title = title,
                artistId = artistId,
                streamUrl = streamUrl,
                scheduledAt = scheduledAt
            )

            // Then
            assertEquals(id, event.id)
            assertEquals(title, event.title)
            assertEquals(artistId, event.artistId)
            assertEquals(streamUrl, event.streamUrl)
            assertEquals(scheduledAt, event.scheduledAt)
            assertNotNull(event.createdAt)
        }

        @Test
        @DisplayName("빈 title로 생성 시 빈 문자열이 허용되는지 확인")
        fun `should handle empty title creation`() {
            // Given
            val emptyTitle = ""
            val artistId = UUID.randomUUID()
            val streamUrl = "https://youtube.com/watch?v=abc123"
            val scheduledAt = Instant.now()

            // When - StreamingEvent는 JPA Entity로 빈 title을 허용함 (validation은 Application Layer에서)
            val event = StreamingEvent(
                title = emptyTitle,
                artistId = artistId,
                streamUrl = streamUrl,
                scheduledAt = scheduledAt
            )

            // Then
            assertEquals(emptyTitle, event.title)
        }

        @Test
        @DisplayName("과거 scheduledAt으로 생성 가능 여부 확인")
        fun `should allow creation with past scheduledAt`() {
            // Given
            val pastScheduledAt = Instant.now().minusSeconds(3600) // 1시간 전
            val artistId = UUID.randomUUID()
            val streamUrl = "https://youtube.com/watch?v=abc123"

            // When - 도메인 모델 레벨에서는 과거 시간도 허용
            val event = StreamingEvent(
                title = "Past Event",
                artistId = artistId,
                streamUrl = streamUrl,
                scheduledAt = pastScheduledAt
            )

            // Then
            assertEquals(pastScheduledAt, event.scheduledAt)
        }

        @Test
        @DisplayName("모든 선택적 필드를 포함하여 생성")
        fun `should create with all optional fields`() {
            // Given
            val id = UUID.randomUUID()
            val title = "Full Featured Event"
            val description = "This is a detailed description"
            val platform = StreamingPlatform.YOUTUBE
            val externalId = "ext-12345"
            val streamUrl = "https://youtube.com/watch?v=xyz789"
            val sourceUrl = "https://original-source.com"
            val thumbnailUrl = "https://thumbnail.com/image.jpg"
            val artistId = UUID.randomUUID()
            val scheduledAt = Instant.now().plusSeconds(7200)
            val startedAt = Instant.now()
            val endedAt = Instant.now().plusSeconds(3600)
            val viewerCount = 1000

            // When
            val event = StreamingEvent(
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
                viewerCount = viewerCount
            )

            // Then
            assertEquals(description, event.description)
            assertEquals(platform, event.platform)
            assertEquals(externalId, event.externalId)
            assertEquals(sourceUrl, event.sourceUrl)
            assertEquals(thumbnailUrl, event.thumbnailUrl)
            assertEquals(startedAt, event.startedAt)
            assertEquals(endedAt, event.endedAt)
            assertEquals(viewerCount, event.viewerCount)
        }
    }

    @Nested
    @DisplayName("StreamingEvent 상태 전이")
    inner class StreamingEventStatusTests {

        @Test
        @DisplayName("기본 상태가 SCHEDULED인지 확인")
        fun `should have SCHEDULED as default status`() {
            // Given & When
            val event = createDefaultEvent()

            // Then
            assertEquals(StreamingStatus.SCHEDULED, event.status)
        }

        @Test
        @DisplayName("SCHEDULED에서 LIVE로 상태 변경 성공")
        fun `should transition from SCHEDULED to LIVE`() {
            // Given
            val event = createDefaultEvent()
            val goLiveTime = Instant.now()

            // When
            event.goLive(goLiveTime)

            // Then
            assertEquals(StreamingStatus.LIVE, event.status)
            assertEquals(goLiveTime, event.startedAt)
        }

        @Test
        @DisplayName("LIVE에서 ENDED로 상태 변경 성공")
        fun `should transition from LIVE to ENDED`() {
            // Given
            val event = createDefaultEvent()
            event.goLive()
            val endTime = Instant.now()

            // When
            event.end(endTime)

            // Then
            assertEquals(StreamingStatus.ENDED, event.status)
            assertEquals(endTime, event.endedAt)
        }

        @Test
        @DisplayName("LIVE가 아닌 상태에서 end() 호출 시 예외 발생")
        fun `should throw when ending non-LIVE event`() {
            // Given
            val scheduledEvent = createDefaultEvent()

            // When & Then - SCHEDULED 상태에서 end() 호출
            val exception = assertThrows<IllegalArgumentException> {
                scheduledEvent.end()
            }
            assertTrue(exception.message?.contains("SCHEDULED") == true)
        }

        @Test
        @DisplayName("SCHEDULED가 아닌 상태에서 goLive() 호출 시 예외 발생")
        fun `should throw when going live from non-SCHEDULED status`() {
            // Given
            val event = createDefaultEvent()
            event.goLive()

            // When & Then - LIVE 상태에서 goLive() 호출
            val exception = assertThrows<IllegalArgumentException> {
                event.goLive()
            }
            assertTrue(exception.message?.contains("LIVE") == true)
        }
    }

    @Nested
    @DisplayName("StreamingEvent 속성 관리")
    inner class StreamingEventPropertyTests {

        @Test
        @DisplayName("platform 설정 및 조회")
        fun `should set and get platform`() {
            // Given
            val event = createDefaultEvent()

            // When
            event.updateSourceIdentity(StreamingPlatform.YOUTUBE, null)

            // Then
            assertEquals(StreamingPlatform.YOUTUBE, event.platform)
        }

        @Test
        @DisplayName("thumbnailUrl 설정 및 조회")
        fun `should set and get thumbnailUrl`() {
            // Given
            val event = createDefaultEvent()
            val newThumbnailUrl = "https://new-thumbnail.com/image.png"

            // When
            event.updateMetadata(event.title, newThumbnailUrl)

            // Then
            assertEquals(newThumbnailUrl, event.thumbnailUrl)
        }

        @Test
        @DisplayName("streamUrl 초기값 조회")
        fun `should get streamUrl`() {
            // Given
            val streamUrl = "https://youtube.com/watch?v=test123"

            // When
            val event = StreamingEvent(
                title = "Test Event",
                artistId = UUID.randomUUID(),
                streamUrl = streamUrl,
                scheduledAt = Instant.now()
            )

            // Then
            assertEquals(streamUrl, event.streamUrl)
        }

        @Test
        @DisplayName("동일 ID StreamingEvent 참조 동등성 확인")
        fun `should verify reference equality for same ID`() {
            // Given
            val sharedId = UUID.randomUUID()
            val artistId = UUID.randomUUID()
            val scheduledAt = Instant.now()

            val event1 = StreamingEvent(
                id = sharedId,
                title = "Event 1",
                artistId = artistId,
                streamUrl = "https://stream1.com",
                scheduledAt = scheduledAt
            )

            val event2 = StreamingEvent(
                id = sharedId,
                title = "Event 2",
                artistId = artistId,
                streamUrl = "https://stream2.com",
                scheduledAt = scheduledAt
            )

            // Then - JPA Entity는 기본적으로 참조 동등성 사용
            // 같은 ID를 가진 다른 인스턴스는 equals에서 다름 (JPA 기본 동작)
            assertNotSame(event1, event2)
            assertEquals(event1.id, event2.id)
        }
    }

    /**
     * Helper method to create a default StreamingEvent for testing
     */
    private fun createDefaultEvent(): StreamingEvent {
        return StreamingEvent(
            title = "Test Event",
            artistId = UUID.randomUUID(),
            streamUrl = "https://youtube.com/watch?v=default",
            scheduledAt = Instant.now().plusSeconds(3600)
        )
    }
}
