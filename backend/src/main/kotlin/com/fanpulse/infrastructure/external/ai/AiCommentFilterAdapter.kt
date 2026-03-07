package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

/**
 * HTTP adapter that calls the Django AI Sidecar's comment filtering API.
 *
 * Implements [CommentFilterPort] using WebClient for non-blocking HTTP calls.
 * Only active when `fanpulse.ai-service.enabled=true` (default).
 *
 * Resilience4j integration:
 * - @CircuitBreaker(name = "aiCommentFilter"): separate circuit breaker for comment filtering
 * - @Retry(name = "aiService"): up to 2 attempts with 500ms wait on failure
 * - Fail-Open fallback: [filterCommentFallback] returns permissive result via [AiServiceFallback]
 *
 * Django API endpoint: POST /api/comments/filter/test
 *
 * JSON is deserialized using snake_case -> camelCase mapping via the shared
 * `aiServiceObjectMapper` configured in [AiServiceConfig].
 */
@Component
@ConditionalOnProperty(name = ["fanpulse.ai-service.enabled"], havingValue = "true", matchIfMissing = true)
class AiCommentFilterAdapter(
    @Qualifier("aiServiceWebClient")
    private val webClient: WebClient,
    private val aiServiceFallback: AiServiceFallback
) : CommentFilterPort {

    companion object {
        private const val COMMENT_FILTER_PATH = "/api/comments/filter/test"
    }

    /**
     * Sends a comment filter check request to Django AI service.
     *
     * Decorated with:
     * - @Retry: retries up to 2 times on failure (config: resilience4j.retry.instances.aiService)
     * - @CircuitBreaker: opens circuit after 60% failure rate; calls [filterCommentFallback] on open
     *
     * Request body: `{"content": "..."}`
     * Response: [CommentFilterResponse] deserialized as [FilterResult]
     *
     * @param content Comment text to filter
     * @return [FilterResult] with filtering decision, or Fail-Open fallback result
     */
    @Retry(name = "aiService")
    @CircuitBreaker(name = "aiCommentFilter", fallbackMethod = "filterCommentFallback")
    override fun filterComment(content: String): FilterResult {
        logger.debug { "Filtering comment (length=${content.length})" }

        val request = CommentFilterRequest(content = content)

        val response = webClient.post()
            .uri(COMMENT_FILTER_PATH)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(CommentFilterResponse::class.java)
            .doOnError { e ->
                logger.warn(e) { "Failed to filter comment: ${e.message}" }
            }
            .block()
            ?: throw AiServiceException("Empty response from comment filter endpoint")

        logger.debug { "Comment filter result: isFiltered=${response.isFiltered}, filterType=${response.filterType}" }

        return response.toDomain()
    }

    // =========================================================================
    // Fallback method (called by @CircuitBreaker on open circuit or exception)
    // =========================================================================

    /**
     * Fallback for [filterComment] when circuit is open or all retries are exhausted.
     * Returns a Fail-Open [FilterResult] via [AiServiceFallback.filterFallback].
     *
     * Method signature must match [filterComment] with an additional [Exception] parameter.
     */
    @Suppress("unused")
    private fun filterCommentFallback(content: String, e: Exception): FilterResult {
        logger.warn { "filterComment fallback triggered for content (length=${content.length}): ${e.javaClass.simpleName}" }
        return aiServiceFallback.filterFallback(content, e)
    }
}

// =============================================================================
// Request / Response DTOs (Django API contract)
// =============================================================================

/**
 * Request body for POST /api/comments/filter/test.
 * Serialized as snake_case JSON by aiServiceObjectMapper.
 */
data class CommentFilterRequest(
    val content: String
)

/**
 * Response from Django comment filter API.
 * Deserialized from snake_case JSON:
 * ```json
 * {"is_filtered": false, "action": null, "rule_id": null, "rule_name": null,
 *  "filter_type": "LLM", "matched_pattern": null, "reason": null}
 * ```
 *
 * Note: Field names here are camelCase; snake_case deserialization is handled
 * by the shared ObjectMapper with PropertyNamingStrategies.SNAKE_CASE.
 */
data class CommentFilterResponse(
    val isFiltered: Boolean = false,
    val action: String? = null,
    val ruleId: Int? = null,
    val ruleName: String? = null,
    val filterType: String = "LLM",
    val matchedPattern: String? = null,
    val reason: String? = null
) {
    /** Converts this response DTO to a domain [FilterResult] value object. */
    fun toDomain(): FilterResult = FilterResult(
        isFiltered = isFiltered,
        filterType = filterType,
        reason = reason,
        ruleName = ruleName
    )
}
