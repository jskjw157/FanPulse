package com.fanpulse.integration

import com.fanpulse.application.service.MetadataRefreshService
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.infrastructure.persistence.streaming.StreamingEventJpaRepository as StreamingEventRepository
import com.fanpulse.domain.streaming.StreamingStatus
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.Instant
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MetadataRefresh Integration Tests")
class MetadataRefreshIntegrationTest {

    companion object {
        private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())

        @JvmStatic
        @BeforeAll
        fun startWireMock() {
            wireMockServer.start()
        }

        @JvmStatic
        @AfterAll
        fun stopWireMock() {
            wireMockServer.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("fanpulse.youtube.oembed.base-url") {
                "http://localhost:${wireMockServer.port()}/oembed"
            }
            registry.add("fanpulse.scheduler.metadata-refresh.enabled") { "false" }
        }
    }

    @Autowired
    private lateinit var metadataRefreshService: MetadataRefreshService

    @Autowired
    private lateinit var repository: StreamingEventRepository

    @BeforeEach
    fun setUp() {
        wireMockServer.resetAll()
        repository.deleteAll()
    }

    @Test
    @DisplayName("should refresh LIVE event metadata from oEmbed API")
    fun shouldRefreshLiveEventMetadata() {
        // given
        val videoId = "testVideo12"
        val event = StreamingEvent(
            id = UUID.randomUUID(),
            title = "Old Title",
            streamUrl = "https://www.youtube.com/embed/$videoId",
            thumbnailUrl = "https://old-thumbnail.jpg",
            artistId = UUID.randomUUID(),
            scheduledAt = Instant.now().minusSeconds(3600),
            startedAt = Instant.now().minusSeconds(1800),
            status = StreamingStatus.LIVE
        )
        repository.save(event)

        wireMockServer.stubFor(
            get(urlPathEqualTo("/oembed"))
                .withQueryParam("url", containing(videoId))
                .withQueryParam("format", equalTo("json"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "title": "New Updated Title",
                                "author_name": "Test Channel",
                                "thumbnail_url": "https://new-thumbnail.jpg",
                                "provider_name": "YouTube"
                            }
                        """.trimIndent())
                )
        )

        // when
        val result = runBlocking { metadataRefreshService.refreshLiveEvents() }

        // then
        assertEquals(1, result.total)
        assertEquals(1, result.updated)
        assertEquals(0, result.failed)

        val updatedEvent = repository.findById(event.id).get()
        assertEquals("New Updated Title", updatedEvent.title)
        assertEquals("https://new-thumbnail.jpg", updatedEvent.thumbnailUrl)
    }

    @Test
    @DisplayName("should handle deleted video gracefully")
    fun shouldHandleDeletedVideoGracefully() {
        // given
        val videoId = "deletedVid1"
        val event = StreamingEvent(
            id = UUID.randomUUID(),
            title = "Title",
            streamUrl = "https://www.youtube.com/embed/$videoId",
            artistId = UUID.randomUUID(),
            scheduledAt = Instant.now(),
            status = StreamingStatus.LIVE
        )
        repository.save(event)

        wireMockServer.stubFor(
            get(urlPathEqualTo("/oembed"))
                .withQueryParam("url", containing(videoId))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withBody("Not Found")
                )
        )

        // when
        val result = runBlocking { metadataRefreshService.refreshLiveEvents() }

        // then
        assertEquals(1, result.total)
        assertEquals(0, result.updated)
        assertEquals(1, result.failed)
    }

    @Test
    @DisplayName("should only refresh non-ENDED events for refreshAllEvents")
    fun shouldOnlyRefreshNonEndedEvents() {
        // given
        val liveVideoId = "liveVideo12"
        val scheduledVideoId = "scheduled01"
        val endedVideoId = "endedVideo1"

        val liveEvent = createEvent(liveVideoId, StreamingStatus.LIVE)
        val scheduledEvent = createEvent(scheduledVideoId, StreamingStatus.SCHEDULED)
        val endedEvent = createEvent(endedVideoId, StreamingStatus.ENDED)

        repository.saveAll(listOf(liveEvent, scheduledEvent, endedEvent))

        // Stub for live and scheduled videos
        stubOEmbedSuccess(liveVideoId, "Updated Live")
        stubOEmbedSuccess(scheduledVideoId, "Updated Scheduled")
        // No stub for ended - should not be called

        // when
        val result = runBlocking { metadataRefreshService.refreshAllEvents() }

        // then
        assertEquals(2, result.total) // Only LIVE and SCHEDULED
        assertEquals(2, result.updated)

        // Verify ended event was not modified
        val unchangedEnded = repository.findById(endedEvent.id).get()
        assertEquals("Old Title", unchangedEnded.title)

        // Verify ENDED video was not called
        wireMockServer.verify(0, getRequestedFor(urlPathEqualTo("/oembed"))
            .withQueryParam("url", containing(endedVideoId)))
    }

    private fun createEvent(videoId: String, status: StreamingStatus): StreamingEvent {
        return StreamingEvent(
            id = UUID.randomUUID(),
            title = "Old Title",
            streamUrl = "https://www.youtube.com/embed/$videoId",
            artistId = UUID.randomUUID(),
            scheduledAt = Instant.now(),
            startedAt = if (status != StreamingStatus.SCHEDULED) Instant.now() else null,
            endedAt = if (status == StreamingStatus.ENDED) Instant.now() else null,
            status = status
        )
    }

    private fun stubOEmbedSuccess(videoId: String, title: String) {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oembed"))
                .withQueryParam("url", containing(videoId))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "title": "$title",
                                "author_name": "Channel",
                                "thumbnail_url": "https://thumb.jpg",
                                "provider_name": "YouTube"
                            }
                        """.trimIndent())
                )
        )
    }
}
