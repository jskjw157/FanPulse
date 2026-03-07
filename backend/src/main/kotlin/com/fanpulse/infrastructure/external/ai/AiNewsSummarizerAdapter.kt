package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.SummaryResult
import com.fanpulse.domain.ai.port.NewsSummarizerPort
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

/**
 * HTTP adapter that calls the Django AI Sidecar's news summarization API.
 *
 * Implements [NewsSummarizerPort] using WebClient for non-blocking HTTP calls.
 * Only active when `fanpulse.ai-service.enabled=true` (default).
 *
 * Resilience4j integration:
 * - @CircuitBreaker(name = "aiSummarizer"): separate circuit breaker (slower AI model inference)
 * - @Retry(name = "aiService"): up to 2 attempts with 500ms wait on failure
 * - Fail-Open fallback: [summarizeFallback] returns empty summary via [AiServiceFallback]
 *
 * Django API endpoint: POST /api/summarize
 *
 * Note: Summarization is slower than moderation (AI model inference).
 * A longer timeout is configured for the aiSummarizer circuit breaker instance.
 *
 * JSON is deserialized using snake_case -> camelCase mapping via the shared
 * `aiServiceObjectMapper` configured in [AiServiceConfig].
 */
@Component
@ConditionalOnProperty(name = ["fanpulse.ai-service.enabled"], havingValue = "true", matchIfMissing = true)
class AiNewsSummarizerAdapter(
    @Qualifier("aiServiceWebClient")
    private val webClient: WebClient,
    private val aiServiceFallback: AiServiceFallback,
    private val aiServiceProperties: AiServiceProperties = AiServiceProperties()
) : NewsSummarizerPort {

    companion object {
        private const val SUMMARIZE_PATH = "/api/summarize"
        private const val DEFAULT_LANGUAGE = "ko"
        private const val DEFAULT_MAX_LENGTH = 200
        private const val DEFAULT_MIN_LENGTH = 50
    }

    /**
     * Sends a news summarization request to Django AI service.
     *
     * Decorated with:
     * - @Retry: retries up to 2 times on failure (config: resilience4j.retry.instances.aiService)
     * - @CircuitBreaker: opens circuit after 60% failure rate; calls [summarizeFallback] on open
     *
     * Request body:
     * ```json
     * {"input_type": "text", "summarize_method": "ai", "text": "...",
     *  "language": "ko", "max_length": 200, "min_length": 50}
     * ```
     * Response: [SummarizeResponse] deserialized as [SummaryResult]
     *
     * @param text News text to summarize
     * @param method Summarization method: "ai" or "extractive"
     * @return [SummaryResult] with summary, bullets, and keywords, or Fail-Open fallback result
     */
    @Retry(name = "aiService")
    @CircuitBreaker(name = "aiSummarizer", fallbackMethod = "summarizeFallback")
    override fun summarize(text: String, method: String): SummaryResult {
        logger.debug { "Summarizing news text (length=${text.length}, method=$method)" }

        val request = SummarizeRequest(
            inputType = "text",
            summarizeMethod = method,
            text = text,
            language = DEFAULT_LANGUAGE,
            maxLength = DEFAULT_MAX_LENGTH,
            minLength = DEFAULT_MIN_LENGTH
        )

        val response = webClient.post()
            .uri(SUMMARIZE_PATH)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(SummarizeResponse::class.java)
            .timeout(aiServiceProperties.timeout.summarizeRead)
            .doOnError { e ->
                logger.warn(e) { "Failed to summarize news text: ${e.message}" }
            }
            .block()
            ?: throw AiServiceException("Empty response from summarize endpoint")

        logger.debug { "Summarization completed in ${response.elapsedMs}ms" }

        return response.toDomain()
    }

    // =========================================================================
    // Fallback method (called by @CircuitBreaker on open circuit or exception)
    // =========================================================================

    /**
     * Fallback for [summarize] when circuit is open or all retries are exhausted.
     * Returns a Fail-Open [SummaryResult] with empty summary via [AiServiceFallback.summaryFallback].
     *
     * Method signature must match [summarize] with an additional [Exception] parameter.
     */
    @Suppress("unused")
    private fun summarizeFallback(text: String, method: String, e: Exception): SummaryResult {
        logger.warn { "summarize fallback triggered for text (length=${text.length}, method=$method): ${e.javaClass.simpleName}" }
        return aiServiceFallback.summaryFallback(text, method, e)
    }
}

// =============================================================================
// Request / Response DTOs (Django API contract)
// =============================================================================

/**
 * Request body for POST /api/summarize.
 * Serialized as snake_case JSON by aiServiceObjectMapper:
 * ```json
 * {"input_type": "text", "summarize_method": "ai", "text": "...",
 *  "language": "ko", "max_length": 200, "min_length": 50}
 * ```
 */
data class SummarizeRequest(
    val inputType: String = "text",
    val summarizeMethod: String = "ai",
    val text: String,
    val language: String = "ko",
    val maxLength: Int = 200,
    val minLength: Int = 50
)

/**
 * Response from Django summarize API.
 * Deserialized from snake_case JSON:
 * ```json
 * {"request_id": "uuid", "summary": "요약 결과",
 *  "bullets": ["핵심 포인트 1"], "keywords": ["키워드1"], "elapsed_ms": 125}
 * ```
 *
 * Note: Field names here are camelCase; snake_case deserialization is handled
 * by the shared ObjectMapper with PropertyNamingStrategies.SNAKE_CASE.
 */
data class SummarizeResponse(
    val requestId: String = "",
    val summary: String = "",
    val bullets: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val elapsedMs: Long? = null
) {
    /** Converts this response DTO to a domain [SummaryResult] value object. */
    fun toDomain(): SummaryResult = SummaryResult(
        summary = summary,
        bullets = bullets,
        keywords = keywords,
        elapsedMs = elapsedMs
    )
}
