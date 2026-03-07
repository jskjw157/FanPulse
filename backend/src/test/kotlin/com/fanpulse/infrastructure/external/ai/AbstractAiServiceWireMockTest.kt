package com.fanpulse.infrastructure.external.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

/**
 * Base class for WireMock-based integration tests of AI service adapters.
 *
 * Provides:
 * - [wireMockServer]: dynamic-port WireMock instance (started/stopped per test)
 * - [webClient]: WebClient configured with snake_case ObjectMapper
 * - [fallback]: shared [AiServiceFallback] instance
 *
 * Subclasses create their own adapter(s) in a separate @BeforeEach method,
 * which JUnit5 executes after this base class's [setUpWireMock].
 */
abstract class AbstractAiServiceWireMockTest {

    protected lateinit var wireMockServer: WireMockServer
    protected lateinit var webClient: WebClient
    protected lateinit var fallback: AiServiceFallback

    @BeforeEach
    fun setUpWireMock() {
        wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
        wireMockServer.start()

        val objectMapper = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerKotlinModule()

        val strategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
                configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
            }
            .build()

        webClient = WebClient.builder()
            .baseUrl("http://localhost:${wireMockServer.port()}")
            .exchangeStrategies(strategies)
            .build()

        fallback = AiServiceFallback()
    }

    @AfterEach
    fun tearDownWireMock() {
        wireMockServer.stop()
    }
}
