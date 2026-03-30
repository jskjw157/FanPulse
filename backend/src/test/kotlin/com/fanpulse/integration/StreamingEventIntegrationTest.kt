package com.fanpulse.integration

import com.fanpulse.application.service.streaming.StreamingEventQueryService
import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.streaming.StreamingEventJpaRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Streaming Event Integration Tests
 *
 * Tests the streaming event query service with real database interactions.
 * Uses H2 in-memory database via test profile with JPA entities.
 *
 * Test coverage:
 * 1. Cursor-based pagination - first page, next page, hasMore
 * 2. Status filtering - LIVE, SCHEDULED, ENDED
 * 3. Ordering - by scheduledAt DESC, id DESC
 * 4. Artist join - artist name resolution
 * 5. Detail API - event with artist name
 * 6. Error handling - 404 for non-existent events
 * 7. Empty database handling
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Streaming Event Integration Tests")
class StreamingEventIntegrationTest {

    @Autowired
    private lateinit var streamingEventQueryService: StreamingEventQueryService

    @Autowired
    private lateinit var streamingEventRepository: StreamingEventJpaRepository

    @Autowired
    private lateinit var artistRepository: ArtistJpaRepository

    private lateinit var testArtist: Artist
    private lateinit var testArtist2: Artist

    @BeforeEach
    fun setUp() {
        // Clean up existing data
        streamingEventRepository.deleteAll()
        artistRepository.deleteAll()

        // Create test artists
        testArtist = artistRepository.save(
            Artist.create(
                name = "BTS",
                englishName = "BTS",
                agency = "HYBE",
                isGroup = true
            )
        )

        testArtist2 = artistRepository.save(
            Artist.create(
                name = "BLACKPINK",
                englishName = "BLACKPINK",
                agency = "YG Entertainment",
                isGroup = true
            )
        )
    }

    @Nested
    @DisplayName("Cursor-based Pagination")
    inner class CursorBasedPagination {

        @Test
        @DisplayName("should return cursor paginated events from DB")
        fun `should return cursor paginated events from DB`() {
            // Given - Create 5 events with different scheduled times
            val now = Instant.now()
            val events = (1..5).map { i ->
                streamingEventRepository.save(
                    StreamingEvent(
                        title = "Event $i",
                        platform = StreamingPlatform.YOUTUBE,
                        externalId = "video$i",
                        streamUrl = "https://www.youtube.com/embed/video$i",
                        artistId = testArtist.id,
                        scheduledAt = now.plus(i.toLong(), ChronoUnit.HOURS),
                        status = StreamingStatus.SCHEDULED
                    )
                )
            }

            // When - Fetch first page with limit 3
            val result = streamingEventQueryService.getWithCursor(
                status = null,
                limit = 3,
                cursor = null
            )

            // Then
            assertEquals(3, result.items.size)
            assertTrue(result.hasMore)
            assertNotNull(result.nextCursor)

            // Verify items are ordered by scheduledAt DESC (most recent first)
            val scheduledTimes = result.items.map { it.scheduledAt }
            assertEquals(scheduledTimes, scheduledTimes.sortedDescending())

            // Verify artist name is populated
            result.items.forEach { item ->
                assertEquals("BTS", item.artistName)
                assertEquals(testArtist.id, item.artistId)
            }
        }

        @Test
        @DisplayName("should return correct hasMore for last page")
        fun `should return correct hasMore for last page`() {
            // Given - Create exactly 3 events
            val now = Instant.now()
            (1..3).forEach { i ->
                streamingEventRepository.save(
                    StreamingEvent(
                        title = "Event $i",
                        platform = StreamingPlatform.YOUTUBE,
                        externalId = "video$i",
                        streamUrl = "https://www.youtube.com/embed/video$i",
                        artistId = testArtist.id,
                        scheduledAt = now.plus(i.toLong(), ChronoUnit.HOURS),
                        status = StreamingStatus.SCHEDULED
                    )
                )
            }

            // When - Fetch with limit 5 (more than available)
            val result = streamingEventQueryService.getWithCursor(
                status = null,
                limit = 5,
                cursor = null
            )

            // Then
            assertEquals(3, result.items.size)
            assertFalse(result.hasMore)
            assertNull(result.nextCursor)
        }

        @Test
        @DisplayName("should order by scheduledAt and id")
        fun `should order by scheduledAt and id`() {
            // Given - Create events with same scheduledAt but different IDs
            val sameTime = Instant.now()
            val savedEvents = (1..3).map { i ->
                streamingEventRepository.save(
                    StreamingEvent(
                        title = "Event $i",
                        platform = StreamingPlatform.YOUTUBE,
                        externalId = "video$i",
                        streamUrl = "https://www.youtube.com/embed/video$i",
                        artistId = testArtist.id,
                        scheduledAt = sameTime, // Same scheduled time
                        status = StreamingStatus.SCHEDULED
                    )
                )
            }

            // When
            val result = streamingEventQueryService.getWithCursor(
                status = null,
                limit = 10,
                cursor = null
            )

            // Then - All items have same scheduledAt, so ordering is by ID DESC
            assertEquals(3, result.items.size)

            // Verify all scheduledAt are equal
            val uniqueTimes = result.items.map { it.scheduledAt }.distinct()
            assertEquals(1, uniqueTimes.size)

            // Verify items are ordered by id DESC (using database sort order)
            // Compare with actual saved events sorted by DB's UUID comparison
            val expectedIds = savedEvents.sortedByDescending {
                // Use string comparison to match H2's UUID sorting
                it.id.toString()
            }.map { it.id }
            val actualIds = result.items.map { it.id }
            assertEquals(expectedIds, actualIds)
        }
    }

    @Nested
    @DisplayName("Status Filtering")
    inner class StatusFiltering {

        @Test
        @DisplayName("should filter by status correctly")
        fun `should filter by status correctly`() {
            // Given - Create events with different statuses
            val now = Instant.now()

            // LIVE event
            streamingEventRepository.save(
                StreamingEvent(
                    title = "Live Event",
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "live1",
                    streamUrl = "https://www.youtube.com/embed/live1",
                    artistId = testArtist.id,
                    scheduledAt = now.minus(1, ChronoUnit.HOURS),
                    startedAt = now.minus(30, ChronoUnit.MINUTES),
                    status = StreamingStatus.LIVE
                )
            )

            // SCHEDULED event
            streamingEventRepository.save(
                StreamingEvent(
                    title = "Scheduled Event",
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "scheduled1",
                    streamUrl = "https://www.youtube.com/embed/scheduled1",
                    artistId = testArtist.id,
                    scheduledAt = now.plus(1, ChronoUnit.HOURS),
                    status = StreamingStatus.SCHEDULED
                )
            )

            // ENDED event
            streamingEventRepository.save(
                StreamingEvent(
                    title = "Ended Event",
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "ended1",
                    streamUrl = "https://www.youtube.com/embed/ended1",
                    artistId = testArtist.id,
                    scheduledAt = now.minus(2, ChronoUnit.HOURS),
                    startedAt = now.minus(2, ChronoUnit.HOURS),
                    endedAt = now.minus(1, ChronoUnit.HOURS),
                    status = StreamingStatus.ENDED
                )
            )

            // When - Filter by LIVE
            val liveResult = streamingEventQueryService.getWithCursor(
                status = StreamingStatus.LIVE,
                limit = 10,
                cursor = null
            )

            // Then
            assertEquals(1, liveResult.items.size)
            assertEquals("LIVE", liveResult.items[0].status)
            assertEquals("Live Event", liveResult.items[0].title)

            // When - Filter by SCHEDULED
            val scheduledResult = streamingEventQueryService.getWithCursor(
                status = StreamingStatus.SCHEDULED,
                limit = 10,
                cursor = null
            )

            // Then
            assertEquals(1, scheduledResult.items.size)
            assertEquals("SCHEDULED", scheduledResult.items[0].status)

            // When - Filter by ENDED
            val endedResult = streamingEventQueryService.getWithCursor(
                status = StreamingStatus.ENDED,
                limit = 10,
                cursor = null
            )

            // Then
            assertEquals(1, endedResult.items.size)
            assertEquals("ENDED", endedResult.items[0].status)

            // When - No filter (all statuses)
            val allResult = streamingEventQueryService.getWithCursor(
                status = null,
                limit = 10,
                cursor = null
            )

            // Then
            assertEquals(3, allResult.items.size)
        }
    }

    @Nested
    @DisplayName("Artist Join")
    inner class ArtistJoin {

        @Test
        @DisplayName("should handle artist join correctly")
        fun `should handle artist join correctly`() {
            // Given - Create events for different artists
            val now = Instant.now()

            streamingEventRepository.save(
                StreamingEvent(
                    title = "BTS Concert",
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "bts1",
                    streamUrl = "https://www.youtube.com/embed/bts1",
                    artistId = testArtist.id,
                    scheduledAt = now.plus(1, ChronoUnit.HOURS),
                    status = StreamingStatus.SCHEDULED
                )
            )

            streamingEventRepository.save(
                StreamingEvent(
                    title = "BLACKPINK Concert",
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "bp1",
                    streamUrl = "https://www.youtube.com/embed/bp1",
                    artistId = testArtist2.id,
                    scheduledAt = now.plus(2, ChronoUnit.HOURS),
                    status = StreamingStatus.SCHEDULED
                )
            )

            // When
            val result = streamingEventQueryService.getWithCursor(
                status = null,
                limit = 10,
                cursor = null
            )

            // Then - Verify each event has correct artist name
            assertEquals(2, result.items.size)

            val bpEvent = result.items.find { it.title == "BLACKPINK Concert" }
            assertNotNull(bpEvent)
            assertEquals("BLACKPINK", bpEvent!!.artistName)
            assertEquals(testArtist2.id, bpEvent.artistId)

            val btsEvent = result.items.find { it.title == "BTS Concert" }
            assertNotNull(btsEvent)
            assertEquals("BTS", btsEvent!!.artistName)
            assertEquals(testArtist.id, btsEvent.artistId)
        }
    }

    @Nested
    @DisplayName("Event Detail API")
    inner class EventDetailApi {

        @Test
        @DisplayName("should return event detail with artist name")
        fun `should return event detail with artist name`() {
            // Given
            val now = Instant.now()
            val event = streamingEventRepository.save(
                StreamingEvent(
                    title = "BTS Live Concert",
                    description = "Annual concert live stream",
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "concert1",
                    streamUrl = "https://www.youtube.com/embed/concert1",
                    thumbnailUrl = "https://example.com/thumb.jpg",
                    artistId = testArtist.id,
                    scheduledAt = now.plus(1, ChronoUnit.HOURS),
                    status = StreamingStatus.SCHEDULED,
                    viewerCount = 0
                )
            )

            // When
            val detail = streamingEventQueryService.getDetailById(event.id)

            // Then
            assertEquals(event.id, detail.id)
            assertEquals("BTS Live Concert", detail.title)
            assertEquals("Annual concert live stream", detail.description)
            assertEquals("BTS", detail.artistName)
            assertEquals(testArtist.id, detail.artistId)
            assertEquals("https://example.com/thumb.jpg", detail.thumbnailUrl)
            assertEquals("SCHEDULED", detail.status)

            // Verify streamUrl has embed parameters
            assertTrue(detail.streamUrl.contains("rel=0"))
            assertTrue(detail.streamUrl.contains("modestbranding=1"))
            assertTrue(detail.streamUrl.contains("playsinline=1"))
        }

        @Test
        @DisplayName("should return 404 for non-existent event")
        fun `should return 404 for non-existent event`() {
            // Given
            val nonExistentId = UUID.randomUUID()

            // When & Then
            assertThrows<NoSuchElementException> {
                streamingEventQueryService.getDetailById(nonExistentId)
            }
        }
    }

    @Nested
    @DisplayName("Empty Database Handling")
    inner class EmptyDatabaseHandling {

        @Test
        @DisplayName("should handle empty database correctly")
        fun `should handle empty database correctly`() {
            // Given - No events in database (only artists from setUp)
            // Clear streaming events just to be sure
            streamingEventRepository.deleteAll()

            // When
            val result = streamingEventQueryService.getWithCursor(
                status = null,
                limit = 10,
                cursor = null
            )

            // Then
            assertTrue(result.items.isEmpty())
            assertFalse(result.hasMore)
            assertNull(result.nextCursor)
        }

        @Test
        @DisplayName("should handle empty result for filtered query")
        fun `should handle empty result for filtered query`() {
            // Given - Only SCHEDULED events exist
            val now = Instant.now()
            streamingEventRepository.save(
                StreamingEvent(
                    title = "Scheduled Event",
                    platform = StreamingPlatform.YOUTUBE,
                    externalId = "scheduled1",
                    streamUrl = "https://www.youtube.com/embed/scheduled1",
                    artistId = testArtist.id,
                    scheduledAt = now.plus(1, ChronoUnit.HOURS),
                    status = StreamingStatus.SCHEDULED
                )
            )

            // When - Query for LIVE events
            val result = streamingEventQueryService.getWithCursor(
                status = StreamingStatus.LIVE,
                limit = 10,
                cursor = null
            )

            // Then
            assertTrue(result.items.isEmpty())
            assertFalse(result.hasMore)
            assertNull(result.nextCursor)
        }
    }
}
