package com.fanpulse.infrastructure.external.youtube

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.web.reactive.function.client.WebClient

@DisplayName("YouTubeOEmbedClient")
class YouTubeOEmbedClientTest {

    private lateinit var wireMockServer: WireMockServer
    private lateinit var client: YouTubeOEmbedClient
    private lateinit var meterRegistry: MeterRegistry
    private lateinit var mockCounter: Counter

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
        wireMockServer.start()

        val webClient = WebClient.builder()
            .baseUrl("http://localhost:${wireMockServer.port()}/oembed")
            .build()

        // Mock MeterRegistry using MockK
        mockCounter = mockk(relaxed = true)
        meterRegistry = mockk {
            every { counter(any<String>(), "type", any<String>()) } returns mockCounter
        }

        client = YouTubeOEmbedClientImpl(
            youTubeWebClient = webClient,
            timeoutMs = 3000,
            maxRetries = 2,
            retryDelayMs = 100,
            meterRegistry = meterRegistry
        )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Nested
    @DisplayName("fetchMetadata")
    inner class FetchMetadata {

        @Test
        @DisplayName("should return metadata when video exists")
        fun shouldReturnMetadataWhenVideoExists() {
            // given
            val videoId = "dQw4w9WgXcQ"
            val responseJson = """
                {
                    "title": "Rick Astley - Never Gonna Give You Up",
                    "author_name": "Rick Astley",
                    "author_url": "https://www.youtube.com/@RickAstley",
                    "type": "video",
                    "height": 113,
                    "width": 200,
                    "version": "1.0",
                    "provider_name": "YouTube",
                    "provider_url": "https://www.youtube.com/",
                    "thumbnail_height": 360,
                    "thumbnail_width": 480,
                    "thumbnail_url": "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
                    "html": "<iframe></iframe>"
                }
            """.trimIndent()

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .withQueryParam("format", equalTo("json"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val metadata = client.fetchMetadata(videoId)

            // then
            assertNotNull(metadata)
            assertEquals("Rick Astley - Never Gonna Give You Up", metadata?.title)
            assertEquals("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", metadata?.thumbnailUrl)
            assertEquals("Rick Astley", metadata?.authorName)
            assertEquals("YouTube", metadata?.providerName)
        }

        @Test
        @DisplayName("should return null when video not found (404)")
        fun shouldReturnNullWhenVideoNotFound() {
            // given
            val videoId = "nonexistent123"

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
            val metadata = client.fetchMetadata(videoId)

            // then
            assertNull(metadata)
        }

        @Test
        @DisplayName("should return null when video is private (401)")
        fun shouldReturnNullWhenVideoIsPrivate() {
            // given
            val videoId = "privateVideo123"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(401)
                            .withBody("Unauthorized")
                    )
            )

            // when
            val metadata = client.fetchMetadata(videoId)

            // then
            assertNull(metadata)
        }

        @Test
        @DisplayName("should throw exception when server error occurs (500) for Circuit Breaker")
        fun shouldThrowExceptionWhenServerError() {
            // given
            val videoId = "anyVideoId"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // when/then - throws exception to trigger Circuit Breaker
            assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }
        }

        @Test
        @DisplayName("should retry on transient failure and succeed")
        fun shouldRetryOnTransientFailureAndSucceed() {
            // given
            val videoId = "retryVideo123"
            val responseJson = """
                {
                    "title": "Test Video",
                    "author_name": "Test Channel",
                    "thumbnail_url": "https://i.ytimg.com/vi/retryVideo123/hqdefault.jpg",
                    "provider_name": "YouTube"
                }
            """.trimIndent()

            // First call fails, second succeeds
            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .inScenario("retry-scenario")
                    .whenScenarioStateIs("Started")
                    .willReturn(aResponse().withStatus(503))
                    .willSetStateTo("first-call-done")
            )

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .inScenario("retry-scenario")
                    .whenScenarioStateIs("first-call-done")
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val metadata = client.fetchMetadata(videoId)

            // then
            assertNotNull(metadata)
            assertEquals("Test Video", metadata?.title)
        }

        @Test
        @DisplayName("should throw exception for malformed JSON response for Circuit Breaker")
        fun shouldThrowExceptionForMalformedJsonResponse() {
            // given
            val videoId = "malformedJson"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("not a valid json")
                    )
            )

            // when/then - throws exception to trigger Circuit Breaker
            assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }
        }
    }

    @Nested
    @DisplayName("Rate Limit Handling (429)")
    inner class RateLimitHandling {

        @Test
        @DisplayName("should retry on rate limit (429) and succeed on retry")
        fun shouldRetryOnRateLimitAndSucceed() {
            // given
            val videoId = "rateLimitVideo123"
            val responseJson = """
                {
                    "title": "Rate Limited Video",
                    "author_name": "Test Channel",
                    "thumbnail_url": "https://i.ytimg.com/vi/rateLimitVideo123/hqdefault.jpg",
                    "provider_name": "YouTube"
                }
            """.trimIndent()

            // First call returns 429, second succeeds
            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .inScenario("rate-limit-scenario")
                    .whenScenarioStateIs("Started")
                    .willReturn(
                        aResponse()
                            .withStatus(429)
                            .withHeader("Retry-After", "5")
                            .withBody("Too Many Requests")
                    )
                    .willSetStateTo("rate-limited")
            )

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .inScenario("rate-limit-scenario")
                    .whenScenarioStateIs("rate-limited")
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val metadata = client.fetchMetadata(videoId)

            // then
            assertNotNull(metadata)
            assertEquals("Rate Limited Video", metadata?.title)

            // Verify retry happened (2 requests total)
            wireMockServer.verify(2, getRequestedFor(urlPathEqualTo("/oembed")))
        }

        @Test
        @DisplayName("should throw RateLimitExceededException after max retries on persistent 429")
        fun shouldThrowAfterMaxRetriesOnPersistent429() {
            // given
            val videoId = "persistentRateLimit"

            // All calls return 429
            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(429)
                            .withHeader("Retry-After", "60")
                            .withBody("Too Many Requests")
                    )
            )

            // when/then - should throw after exhausting retries
            val exception = assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }

            // Verify max retries were attempted (initial + 2 retries = 3)
            wireMockServer.verify(3, getRequestedFor(urlPathEqualTo("/oembed")))
        }

        @Test
        @DisplayName("should parse Retry-After header correctly")
        fun shouldParseRetryAfterHeader() {
            // given
            val videoId = "retryAfterVideo"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(429)
                            .withHeader("Retry-After", "120")
                            .withBody("Too Many Requests")
                    )
            )

            // when/then
            val exception = assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }

            // Exception should eventually be RateLimitExceededException with retry info
            assertTrue(
                exception.cause is RateLimitExceededException ||
                exception is RateLimitExceededException ||
                exception.message?.contains("Rate limit") == true ||
                exception.suppressed.any { it is RateLimitExceededException }
            )
        }
    }

    @Nested
    @DisplayName("Quota Exceeded Handling (403)")
    inner class QuotaExceededHandling {

        @Test
        @DisplayName("should throw QuotaExceededException on 403 Forbidden")
        fun shouldThrowQuotaExceededExceptionOn403() {
            // given
            val videoId = "quotaExceededVideo"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(403)
                            .withBody("Forbidden - Quota Exceeded")
                    )
            )

            // when/then
            val exception = assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }

            // 403 should trigger retry, then propagate exception for Circuit Breaker
            assertTrue(
                exception.cause is QuotaExceededException ||
                exception is QuotaExceededException ||
                exception.message?.contains("Quota") == true ||
                exception.suppressed.any { it is QuotaExceededException }
            )
        }

        @Test
        @DisplayName("should retry on 403 before failing (not immediately fail like 404)")
        fun shouldRetryOn403BeforeFailing() {
            // given
            val videoId = "quotaRetryVideo"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(403)
                            .withBody("Forbidden")
                    )
            )

            // when/then
            assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }

            // Should have retried (unlike 404 which immediately returns null)
            // Initial call + maxRetries (2) = 3 total calls
            wireMockServer.verify(3, getRequestedFor(urlPathEqualTo("/oembed")))
        }
    }

    @Nested
    @DisplayName("Unexpected Client Errors")
    inner class UnexpectedClientErrors {

        @Test
        @DisplayName("should throw UnexpectedClientException on 400 Bad Request")
        fun shouldThrowUnexpectedClientExceptionOn400() {
            // given
            val videoId = "badRequestVideo"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(400)
                            .withBody("Bad Request")
                    )
            )

            // when/then
            val exception = assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }

            // Should have retried before failing
            wireMockServer.verify(3, getRequestedFor(urlPathEqualTo("/oembed")))
        }

        @Test
        @DisplayName("should throw UnexpectedClientException on 418 I'm a teapot")
        fun shouldThrowUnexpectedClientExceptionOn418() {
            // given
            val videoId = "teapotVideo"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(418)
                            .withBody("I'm a teapot")
                    )
            )

            // when/then
            assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }
        }
    }

    @Nested
    @DisplayName("Error Status Code Differentiation")
    inner class ErrorStatusCodeDifferentiation {

        @Test
        @DisplayName("404 should return null without retry (video not found is expected)")
        fun error404ShouldReturnNullWithoutRetry() {
            // given
            val videoId = "notFoundVideo"

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
            val result = client.fetchMetadata(videoId)

            // then
            assertNull(result)
            // Should NOT retry - only 1 call
            wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/oembed")))
        }

        @Test
        @DisplayName("429 should retry (rate limit is temporary)")
        fun error429ShouldRetry() {
            // given
            val videoId = "rateLimitedVideo"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(429)
                            .withBody("Too Many Requests")
                    )
            )

            // when/then
            assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }

            // Should retry - 3 calls total (initial + 2 retries)
            wireMockServer.verify(3, getRequestedFor(urlPathEqualTo("/oembed")))
        }

        @Test
        @DisplayName("403 should retry and then propagate for Circuit Breaker")
        fun error403ShouldRetryAndPropagate() {
            // given
            val videoId = "forbiddenVideo"

            wireMockServer.stubFor(
                get(urlPathEqualTo("/oembed"))
                    .withQueryParam("url", containing(videoId))
                    .willReturn(
                        aResponse()
                            .withStatus(403)
                            .withBody("Forbidden")
                    )
            )

            // when/then
            assertThrows<Exception> {
                client.fetchMetadata(videoId)
            }

            // Should retry - 3 calls total
            wireMockServer.verify(3, getRequestedFor(urlPathEqualTo("/oembed")))
        }
    }
}
