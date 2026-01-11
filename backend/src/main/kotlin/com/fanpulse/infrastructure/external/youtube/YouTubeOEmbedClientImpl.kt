package com.fanpulse.infrastructure.external.youtube

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Implementation of YouTubeOEmbedClient using WebClient with Circuit Breaker.
 */
@Component
class YouTubeOEmbedClientImpl(
    private val youTubeWebClient: WebClient,
    @Value("\${fanpulse.youtube.oembed.timeout-ms:5000}")
    private val timeoutMs: Long,
    @Value("\${fanpulse.youtube.oembed.retry.max-attempts:3}")
    private val maxRetries: Int,
    @Value("\${fanpulse.youtube.oembed.retry.delay-ms:1000}")
    private val retryDelayMs: Long,
    private val meterRegistry: MeterRegistry
) : YouTubeOEmbedClient {

    companion object {
        private const val METRIC_PREFIX = "youtube.oembed.error"
    }

    @CircuitBreaker(name = "youtubeOEmbed", fallbackMethod = "fallbackFetchMetadata")
    override fun fetchMetadata(videoId: String): YouTubeMetadata? {
        val watchUrl = "https://www.youtube.com/watch?v=$videoId"

        return try {
            val response = youTubeWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .queryParam("url", watchUrl)
                        .queryParam("format", "json")
                        .build()
                }
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { clientResponse ->
                    handleClientError(clientResponse, videoId)
                }
                .bodyToMono(OEmbedResponse::class.java)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(
                    Retry.backoff(maxRetries.toLong(), Duration.ofMillis(retryDelayMs))
                        .filter { throwable ->
                            // Only retry on retryable exceptions (not VideoNotFoundException)
                            throwable !is VideoNotFoundException
                        }
                        .doBeforeRetry { signal ->
                            logger.debug { "Retrying fetch for video $videoId, attempt ${signal.totalRetries() + 1}" }
                        }
                )
                .block()

            response?.let {
                YouTubeMetadata(
                    title = it.title,
                    thumbnailUrl = it.thumbnailUrl,
                    authorName = it.authorName,
                    providerName = it.providerName
                )
            }
        } catch (e: VideoNotFoundException) {
            logger.debug { "Video not found: $videoId" }
            null
        } catch (e: WebClientResponseException) {
            logger.warn(e) { "Failed to fetch metadata for video $videoId: ${e.statusCode}" }
            throw e // Re-throw to trigger Circuit Breaker
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error fetching metadata for video $videoId" }
            throw e // Re-throw to trigger Circuit Breaker
        }
    }

    /**
     * Handle 4xx client errors with specific logic per status code.
     */
    private fun handleClientError(response: ClientResponse, videoId: String): Mono<Throwable> {
        val statusCode = response.statusCode().value()

        return when (statusCode) {
            404, 401 -> {
                // Video not found or private - normal business case
                incrementErrorMetric("not_found")
                logger.debug { "Video $videoId not found or private: $statusCode" }
                Mono.error(VideoNotFoundException(videoId))
            }
            429 -> {
                // Rate Limit - should be retried with backoff
                incrementErrorMetric("rate_limit")
                val retryAfterSeconds = response.headers().header("Retry-After").firstOrNull()?.toLongOrNull()
                logger.warn { "Rate limited for video $videoId, retry-after: ${retryAfterSeconds ?: "not specified"}s" }
                Mono.error(RateLimitExceededException(videoId, retryAfterSeconds))
            }
            403 -> {
                // Forbidden - quota exceeded or access denied, should trigger Circuit Breaker
                incrementErrorMetric("quota_exceeded")
                logger.warn { "Forbidden access for video $videoId (possible quota exceeded)" }
                Mono.error(QuotaExceededException(videoId))
            }
            else -> {
                // Other 4xx - unexpected client error
                incrementErrorMetric("unexpected_client_error")
                logger.error { "Unexpected client error for video $videoId: $statusCode" }
                Mono.error(UnexpectedClientException(videoId, statusCode))
            }
        }
    }

    /**
     * Increment error metric counter.
     */
    private fun incrementErrorMetric(errorType: String) {
        meterRegistry.counter(METRIC_PREFIX, "type", errorType).increment()
    }

    /**
     * Fallback method when Circuit Breaker is open.
     */
    @Suppress("unused")
    private fun fallbackFetchMetadata(videoId: String, e: Exception): YouTubeMetadata? {
        logger.warn { "Circuit breaker fallback triggered for video $videoId: ${e.message}" }
        return null
    }
}

// =============================================================================
// Exception Hierarchy
// =============================================================================

/**
 * Base exception for YouTube API errors.
 */
sealed class YouTubeApiException(message: String) : RuntimeException(message)

/**
 * Exception thrown when video is not found (404) or private (401).
 * This is a normal business case and should NOT trigger retry or Circuit Breaker.
 */
class VideoNotFoundException(val videoId: String) : YouTubeApiException("Video not found: $videoId")

/**
 * Exception thrown when rate limit is exceeded (429).
 * This should trigger retry with exponential backoff.
 */
class RateLimitExceededException(
    val videoId: String,
    val retryAfterSeconds: Long? = null
) : YouTubeApiException("Rate limit exceeded for video: $videoId${retryAfterSeconds?.let { ", retry after ${it}s" } ?: ""}")

/**
 * Exception thrown when quota is exceeded or access is forbidden (403).
 * This should trigger Circuit Breaker to prevent further requests.
 */
class QuotaExceededException(val videoId: String) : YouTubeApiException("Quota exceeded or forbidden for video: $videoId")

/**
 * Exception thrown for unexpected 4xx client errors.
 * This should be logged and propagated for investigation.
 */
class UnexpectedClientException(
    val videoId: String,
    val statusCode: Int
) : YouTubeApiException("Unexpected client error ($statusCode) for video: $videoId")

/**
 * oEmbed API response structure.
 */
data class OEmbedResponse(
    val title: String = "",
    @JsonProperty("author_name")
    val authorName: String = "",
    @JsonProperty("author_url")
    val authorUrl: String = "",
    val type: String = "",
    val height: Int = 0,
    val width: Int = 0,
    val version: String = "",
    @JsonProperty("provider_name")
    val providerName: String = "",
    @JsonProperty("provider_url")
    val providerUrl: String = "",
    @JsonProperty("thumbnail_height")
    val thumbnailHeight: Int = 0,
    @JsonProperty("thumbnail_width")
    val thumbnailWidth: Int = 0,
    @JsonProperty("thumbnail_url")
    val thumbnailUrl: String = "",
    val html: String = ""
)
