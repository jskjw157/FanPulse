package com.fanpulse.infrastructure.external.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Configuration properties for the Django AI Sidecar service.
 *
 * Bound from `fanpulse.ai-service.*` in application.yml.
 *
 * @property enabled Whether the AI service integration is enabled (Feature Flag)
 * @property baseUrl Base URL of the Django AI service
 * @property timeout Timeout settings for different types of requests
 */
@ConfigurationProperties(prefix = "fanpulse.ai-service")
data class AiServiceProperties(
    val enabled: Boolean = true,
    val baseUrl: String = "http://localhost:8001",
    val apiKey: String = "",
    val timeout: AiServiceTimeout = AiServiceTimeout()
)

/**
 * Timeout configuration for AI service requests.
 *
 * @property connect Connection timeout
 * @property read Default read timeout (for moderation/filter)
 * @property summarizeRead Extended read timeout for summarization (AI model is slower)
 */
data class AiServiceTimeout(
    val connect: Duration = Duration.ofSeconds(3),
    val read: Duration = Duration.ofSeconds(5),
    val summarizeRead: Duration = Duration.ofSeconds(30)
)

/**
 * Spring configuration for Django AI Sidecar WebClient beans.
 *
 * Creates a shared WebClient bean with snake_case Jackson ObjectMapper
 * to correctly deserialize Django API responses.
 */
@Configuration
@EnableConfigurationProperties(AiServiceProperties::class)
class AiServiceConfig(
    private val aiServiceProperties: AiServiceProperties
) {
    init {
        require(aiServiceProperties.apiKey.isNotBlank() || !aiServiceProperties.enabled) {
            "fanpulse.ai-service.api-key (AI_SERVICE_API_KEY) must be set when ai-service is enabled"
        }
    }

    /**
     * Shared ObjectMapper configured for Django API snake_case JSON convention.
     *
     * Django uses snake_case field names (e.g., `is_flagged`, `model_used`)
     * while Kotlin uses camelCase (e.g., `isFlagged`, `modelUsed`).
     * This mapper handles the conversion automatically.
     */
    @Bean(name = ["aiServiceObjectMapper"])
    fun aiServiceObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerKotlinModule()
    }

    /**
     * WebClient configured for the Django AI Sidecar service.
     *
     * Features:
     * - Base URL from `fanpulse.ai-service.base-url`
     * - Jackson snake_case codec for request/response serialization
     * - Netty HttpClient with connect + read timeouts from AiServiceProperties
     * - Used by all AI adapter implementations
     */
    @Bean(name = ["aiServiceWebClient"])
    fun aiServiceWebClient(aiServiceObjectMapper: ObjectMapper): WebClient {
        logger.info { "Configuring AI service WebClient with base URL: ${aiServiceProperties.baseUrl}" }

        val httpClient = HttpClient.create()
            .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS,
                aiServiceProperties.timeout.connect.toMillis().toInt())
            .responseTimeout(aiServiceProperties.timeout.read)

        val strategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().jackson2JsonDecoder(
                    Jackson2JsonDecoder(aiServiceObjectMapper)
                )
                configurer.defaultCodecs().jackson2JsonEncoder(
                    Jackson2JsonEncoder(aiServiceObjectMapper)
                )
            }
            .build()

        return WebClient.builder()
            .baseUrl(aiServiceProperties.baseUrl)
            .defaultHeader("X-Api-Key", aiServiceProperties.apiKey)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .build()
    }
}
