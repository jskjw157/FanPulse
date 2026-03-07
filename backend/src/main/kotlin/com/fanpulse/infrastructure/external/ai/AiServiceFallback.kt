package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.ModerationResult
import com.fanpulse.domain.ai.SummaryResult
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Fail-Open fallback implementation for the Django AI Sidecar service.
 *
 * When the AI service is unavailable (timeout, circuit open, connection error),
 * all methods return permissive results that **allow** content to pass through.
 *
 * Fail-Open strategy rationale:
 * - User experience takes priority over strict AI moderation during outages
 * - Blocking all content when AI is down causes unacceptable UX degradation
 * - Human moderation remains as the last line of defense
 *
 * All fallback results are identifiable via:
 * - [ModerationResult.modelUsed] = "fallback"
 * - [FilterResult.filterType] = "fallback"
 * - [SummaryResult.error] = "AI service unavailable"
 */
@Component
class AiServiceFallback {

    /**
     * Fallback for content moderation when AI service is unavailable.
     *
     * Returns a permissive result that allows the content:
     * - isFlagged = false (content is NOT flagged)
     * - action = "allow" (content is allowed through)
     * - modelUsed = "fallback" (identifies this as a fallback response)
     *
     * @param text The content that was being checked (unused in fallback)
     * @param e The exception that triggered the fallback
     * @return [ModerationResult] with Fail-Open permissive values
     */
    fun moderationFallback(text: String, e: Exception): ModerationResult {
        logger.warn { "Moderation fallback triggered for text (length=${text.length}): ${e.message}" }
        return ModerationResult(
            isFlagged = false,
            action = "allow",
            highestCategory = null,
            highestScore = null,
            confidence = 0.0,
            modelUsed = "fallback",
            processingTimeMs = null,
            error = e.message
        )
    }

    /**
     * Fallback for batch content moderation when AI service is unavailable.
     *
     * Returns a list of permissive results, one per input text.
     *
     * @param texts The list of contents that were being checked
     * @param e The exception that triggered the fallback
     * @return List of [ModerationResult] with Fail-Open permissive values
     */
    fun batchModerationFallback(texts: List<String>, e: Exception): List<ModerationResult> {
        logger.warn { "Batch moderation fallback triggered for ${texts.size} texts: ${e.message}" }
        return texts.map { moderationFallback(it, e) }
    }

    /**
     * Fallback for comment filtering when AI service is unavailable.
     *
     * Returns a permissive result that allows the comment:
     * - isFiltered = false (comment is NOT filtered)
     * - filterType = "fallback" (identifies this as a fallback response)
     *
     * @param content The comment content that was being filtered (unused in fallback)
     * @param e The exception that triggered the fallback
     * @return [FilterResult] with Fail-Open permissive values
     */
    fun filterFallback(content: String, e: Exception): FilterResult {
        logger.warn { "Comment filter fallback triggered for content (length=${content.length}): ${e.message}" }
        return FilterResult(
            isFiltered = false,
            filterType = "fallback",
            reason = null,
            ruleName = null
        )
    }

    /**
     * Fallback for news summarization when AI service is unavailable.
     *
     * Returns an empty summary with an error message:
     * - summary = "" (empty, no summary available)
     * - error = "AI service unavailable" (indicates the failure reason)
     *
     * @param text The news text that was being summarized (unused in fallback)
     * @param method The summarization method (unused in fallback)
     * @param e The exception that triggered the fallback
     * @return [SummaryResult] with empty summary and error indicator
     */
    fun summaryFallback(text: String, method: String, e: Exception): SummaryResult {
        logger.warn { "News summarizer fallback triggered for text (length=${text.length}): ${e.message}" }
        return SummaryResult(
            summary = "",
            bullets = emptyList(),
            keywords = emptyList(),
            elapsedMs = null,
            error = "AI service unavailable"
        )
    }
}
