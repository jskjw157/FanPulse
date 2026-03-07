package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.ModerationResult
import com.fanpulse.domain.ai.port.ContentModerationPort
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

/**
 * HTTP adapter that calls the Django AI Sidecar's content moderation API.
 *
 * Implements [ContentModerationPort] using WebClient for non-blocking HTTP calls.
 * Only active when `fanpulse.ai-service.enabled=true` (default).
 *
 * Resilience4j integration:
 * - @CircuitBreaker(name = "aiModeration"): opens after 60% failure rate over 10 calls
 * - @Retry(name = "aiService"): up to 2 attempts with 500ms wait on failure
 * - Fail-Open fallback: all fallback methods return permissive results via [AiServiceFallback]
 *
 * Django API endpoints:
 * - Single check: POST /api/moderation/check
 * - Batch check:  POST /api/moderation/batch
 *
 * JSON is deserialized using snake_case -> camelCase mapping via the shared
 * `aiServiceObjectMapper` configured in [AiServiceConfig].
 */
@Component
@ConditionalOnProperty(name = ["fanpulse.ai-service.enabled"], havingValue = "true", matchIfMissing = true)
class AiModerationAdapter(
    @Qualifier("aiServiceWebClient")
    private val webClient: WebClient,
    private val aiServiceFallback: AiServiceFallback
) : ContentModerationPort {

    companion object {
        private const val MODERATION_CHECK_PATH = "/api/moderation/check"
        private const val MODERATION_BATCH_PATH = "/api/moderation/batch"
    }

    /**
     * Sends a single content moderation check request to Django AI service.
     *
     * Decorated with:
     * - @Retry: retries up to 2 times on failure (config: resilience4j.retry.instances.aiService)
     * - @CircuitBreaker: opens circuit after 60% failure rate; calls [checkContentFallback] on open
     *
     * Request body: `{"text": "...", "use_cache": true}`
     * Response: [ModerationCheckResponse] deserialized as [ModerationResult]
     *
     * @param text Text content to check
     * @return [ModerationResult] with moderation decision, or Fail-Open fallback result
     */
    @Retry(name = "aiService")
    @CircuitBreaker(name = "aiModeration", fallbackMethod = "checkContentFallback")
    override fun checkContent(text: String): ModerationResult {
        logger.debug { "Checking content moderation for text (length=${text.length})" }

        val request = ModerationCheckRequest(text = text)

        val response = webClient.post()
            .uri(MODERATION_CHECK_PATH)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ModerationCheckResponse::class.java)
            .doOnError { e ->
                logger.warn(e) { "Failed to check content moderation: ${e.message}" }
            }
            .block()
            ?: throw AiServiceException("Empty response from moderation check endpoint")

        logger.debug { "Moderation check result: isFlagged=${response.isFlagged}, action=${response.action}" }

        return response.toDomain()
    }

    /**
     * Sends a batch content moderation check request to Django AI service.
     *
     * Decorated with:
     * - @Retry: retries up to 2 times on failure (config: resilience4j.retry.instances.aiService)
     * - @CircuitBreaker: opens circuit after 60% failure rate; calls [batchCheckFallback] on open
     *
     * Request body: `{"texts": ["...", "..."]}`
     * Response: Array of [ModerationCheckResponse] deserialized as [List<ModerationResult>]
     *
     * @param texts List of text contents to check
     * @return List of [ModerationResult] in the same order as input, or Fail-Open fallback results
     */
    @Retry(name = "aiService")
    @CircuitBreaker(name = "aiModeration", fallbackMethod = "batchCheckFallback")
    override fun batchCheck(texts: List<String>): List<ModerationResult> {
        if (texts.isEmpty()) {
            logger.debug { "Empty batch, returning empty results" }
            return emptyList()
        }

        logger.debug { "Batch checking moderation for ${texts.size} texts" }

        val request = ModerationBatchRequest(texts = texts)

        val responses = webClient.post()
            .uri(MODERATION_BATCH_PATH)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(ModerationCheckResponse::class.java)
            .collectList()
            .doOnError { e ->
                logger.warn(e) { "Failed to batch check content moderation: ${e.message}" }
            }
            .block() ?: emptyList()

        logger.debug { "Batch moderation check completed: ${responses.size} results" }

        return responses.map { it.toDomain() }
    }

    // =========================================================================
    // Fallback methods (called by @CircuitBreaker on open circuit or exception)
    // Fail-Open strategy: all fallbacks return permissive results
    // =========================================================================

    /**
     * Fallback for [checkContent] when circuit is open or all retries are exhausted.
     * Returns a Fail-Open [ModerationResult] via [AiServiceFallback.moderationFallback].
     *
     * Method signature must match [checkContent] with an additional [Exception] parameter.
     */
    @Suppress("unused")
    private fun checkContentFallback(text: String, e: Exception): ModerationResult {
        logger.warn { "checkContent fallback triggered for text (length=${text.length}): ${e.javaClass.simpleName}" }
        return aiServiceFallback.moderationFallback(text, e)
    }

    /**
     * Fallback for [batchCheck] when circuit is open or all retries are exhausted.
     * Returns a Fail-Open list of [ModerationResult] via [AiServiceFallback.batchModerationFallback].
     *
     * Method signature must match [batchCheck] with an additional [Exception] parameter.
     */
    @Suppress("unused")
    private fun batchCheckFallback(texts: List<String>, e: Exception): List<ModerationResult> {
        logger.warn { "batchCheck fallback triggered for ${texts.size} texts: ${e.javaClass.simpleName}" }
        return aiServiceFallback.batchModerationFallback(texts, e)
    }
}

// =============================================================================
// Request / Response DTOs (Django API contract)
// =============================================================================

/**
 * Request body for POST /api/moderation/check.
 * Serialized as snake_case JSON by aiServiceObjectMapper.
 */
data class ModerationCheckRequest(
    val text: String,
    val useCache: Boolean = true
)

/**
 * Request body for POST /api/moderation/batch.
 * Serialized as snake_case JSON by aiServiceObjectMapper.
 */
data class ModerationBatchRequest(
    val texts: List<String>
)

/**
 * Response from Django moderation API.
 * Deserialized from snake_case JSON:
 * ```json
 * {"is_flagged": false, "action": "allow", "highest_category": null,
 *  "highest_score": 0.1, "confidence": 0.9, "model_used": "ko",
 *  "processing_time_ms": 38, "cached": false, "error": null}
 * ```
 *
 * Note: Field names here are camelCase; snake_case deserialization is handled
 * by the shared ObjectMapper with PropertyNamingStrategies.SNAKE_CASE.
 */
data class ModerationCheckResponse(
    val isFlagged: Boolean = false,
    val action: String = "allow",
    val highestCategory: String? = null,
    val highestScore: Double? = null,
    val confidence: Double = 0.0,
    val modelUsed: String = "unknown",
    val processingTimeMs: Long? = null,
    val cached: Boolean = false,
    val error: String? = null
) {
    /** Converts this response DTO to a domain [ModerationResult] value object. */
    fun toDomain(): ModerationResult = ModerationResult(
        isFlagged = isFlagged,
        action = action,
        highestCategory = highestCategory,
        highestScore = highestScore,
        confidence = confidence,
        modelUsed = modelUsed,
        processingTimeMs = processingTimeMs,
        error = error
    )
}
