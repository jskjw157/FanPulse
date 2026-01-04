package com.fanpulse.infrastructure.external.youtube

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.web.reactive.function.client.WebClient

@DisplayName("YouTubeOEmbedClient")
class YouTubeOEmbedClientTest {

    private lateinit var wireMockServer: WireMockServer
    private lateinit var client: YouTubeOEmbedClient

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
        wireMockServer.start()

        val webClient = WebClient.builder()
            .baseUrl("http://localhost:${wireMockServer.port()}/oembed")
            .build()

        client = YouTubeOEmbedClientImpl(
            webClient = webClient,
            timeoutMs = 3000,
            maxRetries = 2,
            retryDelayMs = 100
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
        @DisplayName("should return null when server error occurs (500)")
        fun shouldReturnNullWhenServerError() {
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

            // when
            val metadata = client.fetchMetadata(videoId)

            // then
            assertNull(metadata)
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
        @DisplayName("should handle malformed JSON response")
        fun shouldHandleMalformedJsonResponse() {
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

            // when
            val metadata = client.fetchMetadata(videoId)

            // then
            assertNull(metadata)
        }
    }
}
