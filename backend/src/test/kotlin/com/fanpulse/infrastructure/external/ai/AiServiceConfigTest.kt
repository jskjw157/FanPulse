package com.fanpulse.infrastructure.external.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

/**
 * Tests for API Key header transmission and fail-fast validation.
 *
 * Phase 2A:
 * - WebClient sends X-Api-Key header on every request
 * - AiServiceConfig fails to init when apiKey is blank + enabled=true
 * - AiServiceConfig succeeds when apiKey is blank + enabled=false
 */
@DisplayName("AiServiceConfig - API Key Header + Fail-Fast")
class AiServiceConfigTest : AbstractAiServiceWireMockTest() {

    // =========================================================================
    // Test 2A.1: API Key Header Transmission
    // =========================================================================

    @Nested
    @DisplayName("API Key Header Transmission")
    inner class ApiKeyHeaderTransmission {

        @Test
        @DisplayName("should send X-Api-Key header on moderation request")
        fun shouldSendApiKeyHeaderOnModerationRequest() {
            val testApiKey = "test-api-key-12345"
            // Phase 2B: updated to /api/ai/moderate
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .withHeader("X-Api-Key", equalTo(testApiKey))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "is_flagged": false,
                                    "action": "allow",
                                    "highest_category": null,
                                    "highest_score": 0.05,
                                    "confidence": 0.98,
                                    "model_used": "ko",
                                    "processing_time_ms": 25,
                                    "cached": false,
                                    "error": null
                                }
                            """.trimIndent())
                    )
            )

            val apiKeyWebClient = buildWebClientWithApiKey(testApiKey)
            val adapter = AiModerationAdapter(apiKeyWebClient, fallback)
            val result = adapter.checkContent("clean content")

            wireMockServer.verify(
                postRequestedFor(urlEqualTo("/api/ai/moderate"))
                    .withHeader("X-Api-Key", equalTo(testApiKey))
            )
            assertFalse(result.isFlagged)
        }

        @Test
        @DisplayName("should send X-Api-Key header on summarize request")
        fun shouldSendApiKeyHeaderOnSummarizeRequest() {
            val testApiKey = "test-api-key-67890"
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/summarize"))
                    .withHeader("X-Api-Key", equalTo(testApiKey))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "request_id": "req-123",
                                    "summary": "Test summary",
                                    "bullets": [],
                                    "keywords": [],
                                    "elapsed_ms": 100
                                }
                            """.trimIndent())
                    )
            )

            val apiKeyWebClient = buildWebClientWithApiKey(testApiKey)
            val props = AiServiceProperties(enabled = true, apiKey = testApiKey)
            val adapter = AiNewsSummarizerAdapter(apiKeyWebClient, fallback, props)
            val result = adapter.summarize("Some news article text", "ai")

            wireMockServer.verify(
                postRequestedFor(urlEqualTo("/api/ai/summarize"))
                    .withHeader("X-Api-Key", equalTo(testApiKey))
            )
            assertEquals("Test summary", result.summary)
        }

        @Test
        @DisplayName("should send X-Api-Key header on comment filter request")
        fun shouldSendApiKeyHeaderOnFilterRequest() {
            val testApiKey = "test-api-key-filter"
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .withHeader("X-Api-Key", equalTo(testApiKey))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "is_filtered": false,
                                    "filter_type": "none",
                                    "reason": null
                                }
                            """.trimIndent())
                    )
            )

            val apiKeyWebClient = buildWebClientWithApiKey(testApiKey)
            val adapter = AiCommentFilterAdapter(apiKeyWebClient, fallback)
            val result = adapter.filterComment("normal comment")

            wireMockServer.verify(
                postRequestedFor(urlEqualTo("/api/ai/filter"))
                    .withHeader("X-Api-Key", equalTo(testApiKey))
            )
            assertFalse(result.isFiltered)
        }
    }

    // =========================================================================
    // Test 2A.2: Fail-Fast Validation (via AiServiceConfig)
    // =========================================================================

    @Nested
    @DisplayName("Fail-Fast Validation")
    inner class FailFastValidation {

        @Test
        @DisplayName("should throw when apiKey is blank and service is enabled")
        fun shouldThrowWhenApiKeyBlankAndEnabled() {
            val props = AiServiceProperties(enabled = true, apiKey = "")
            assertThrows<IllegalArgumentException> {
                AiServiceConfig(props)
            }
        }

        @Test
        @DisplayName("should not throw when apiKey is blank and service is disabled")
        fun shouldNotThrowWhenApiKeyBlankAndDisabled() {
            val props = AiServiceProperties(enabled = false, apiKey = "")
            assertDoesNotThrow { AiServiceConfig(props) }
        }

        @Test
        @DisplayName("should accept valid apiKey when service is enabled")
        fun shouldAcceptValidApiKeyWhenEnabled() {
            val props = AiServiceProperties(enabled = true, apiKey = "valid-key-123")
            assertDoesNotThrow { AiServiceConfig(props) }
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private fun buildWebClientWithApiKey(apiKey: String): WebClient {
        val objectMapper = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerModule(KotlinModule.Builder().build())

        val strategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
                configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            }
            .build()

        return WebClient.builder()
            .baseUrl("http://localhost:${wireMockServer.port()}")
            .defaultHeader("X-Api-Key", apiKey)
            .exchangeStrategies(strategies)
            .build()
    }
}
