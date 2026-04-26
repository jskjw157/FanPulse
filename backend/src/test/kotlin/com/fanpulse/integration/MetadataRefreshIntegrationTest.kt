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

    @Test
    @DisplayName("should isolate transactions between events (REQUIRES_NEW): success commits independently when others fail")
    fun shouldIsolateTransactionsBetweenEvents() {
        // given: 두 이벤트 — 하나는 oEmbed 성공, 하나는 5xx 실패
        val successVideoId = "successVid1"
        val failureVideoId = "failureVid1"

        val successEvent = createEvent(successVideoId, StreamingStatus.LIVE)
        val failureEvent = createEvent(failureVideoId, StreamingStatus.LIVE)
        repository.saveAll(listOf(successEvent, failureEvent))

        // 첫 번째 이벤트: 정상 응답 → updateEventMetadata 성공 → REQUIRES_NEW 트랜잭션 commit
        stubOEmbedSuccess(successVideoId, "Successfully Updated")

        // 두 번째 이벤트: 500 에러 → updateEventMetadata 실패 → REQUIRES_NEW 트랜잭션 rollback
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oembed"))
                .withQueryParam("url", containing(failureVideoId))
                .willReturn(aResponse().withStatus(500))
        )

        // when: 배치 갱신 실행 (refreshEvents 내부에서 각 이벤트가 독립 트랜잭션으로 처리됨)
        val result = runBlocking { metadataRefreshService.refreshLiveEvents() }

        // then: REQUIRES_NEW 격리 효과 검증

        // 1) 통계 카운터 — 부분 실패가 전체 배치를 중단시키지 않음을 증명
        assertEquals(2, result.total, "두 이벤트 모두 처리 시도되어야 함")
        assertEquals(1, result.updated, "성공한 이벤트는 1건이어야 함")
        assertEquals(1, result.failed, "실패한 이벤트는 1건이어야 함")
        assertEquals(1, result.errors.size, "실패 이벤트의 errors 항목이 누적되어야 함")
        assertEquals(failureEvent.id, result.errors[0].eventId, "errors의 eventId는 실패한 이벤트여야 함")

        // 2) 성공 이벤트는 DB에 실제로 commit됨 (REQUIRES_NEW의 핵심)
        //    — 만약 외부 트랜잭션이 모든 이벤트를 감싸고 있다면, 한 이벤트의 실패가
        //    전체 트랜잭션을 rollback 시켜 이 어서션이 깨질 수 있음.
        //    따라서 이 어서션은 REQUIRES_NEW 격리의 회귀 가드 역할.
        val updatedSuccess = repository.findById(successEvent.id).orElseThrow()
        assertEquals(
            "Successfully Updated",
            updatedSuccess.title,
            "성공 이벤트는 다른 이벤트 실패와 무관하게 DB에 commit되어야 함 (REQUIRES_NEW)"
        )

        // 3) 실패 이벤트는 원본 상태 유지 — REQUIRES_NEW 트랜잭션 rollback 검증
        val unchangedFailure = repository.findById(failureEvent.id).orElseThrow()
        assertEquals(
            "Old Title",
            unchangedFailure.title,
            "실패한 이벤트는 변경되지 않아야 함 (트랜잭션 rollback)"
        )
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
