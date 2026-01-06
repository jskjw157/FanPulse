package com.fanpulse.infrastructure.external.youtube

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
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
    private val retryDelayMs: Long
) : YouTubeOEmbedClient {

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
                .onStatus(HttpStatusCode::is4xxClientError) { response ->
                    // 404, 401 etc - video not found or private
                    logger.debug { "Video $videoId not found or private: ${response.statusCode()}" }
                    throw VideoNotFoundException(videoId)
                }
                .bodyToMono(OEmbedResponse::class.java)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(
                    Retry.backoff(maxRetries.toLong(), Duration.ofMillis(retryDelayMs))
                        .filter { it !is VideoNotFoundException }
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
     * Fallback method when Circuit Breaker is open.
     */
    @Suppress("unused")
    private fun fallbackFetchMetadata(videoId: String, e: Exception): YouTubeMetadata? {
        logger.warn { "Circuit breaker fallback triggered for video $videoId: ${e.message}" }
        return null
    }
}

/**
 * Exception thrown when video is not found (404) or private (401).
 */
class VideoNotFoundException(videoId: String) : RuntimeException("Video not found: $videoId")

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
