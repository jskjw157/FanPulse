package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.SummaryResult
import com.fanpulse.domain.ai.port.NewsSummarizerPort
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * No-Op implementation of [NewsSummarizerPort] used when the AI service is disabled.
 *
 * Active only when `fanpulse.ai-service.enabled=false`.
 * When the AI service feature flag is turned off, this bean replaces [AiNewsSummarizerAdapter]
 * and returns an empty summary with an error indicator without making any HTTP calls.
 *
 * Fail-Open strategy:
 * - [summarize] → summary="", error="AI service disabled"
 *
 * @see AiNewsSummarizerAdapter for the real implementation
 * @see NewsSummarizerPort for the port contract
 */
@Component
@ConditionalOnProperty(name = ["fanpulse.ai-service.enabled"], havingValue = "false")
class NoOpNewsSummarizerAdapter : NewsSummarizerPort {

    /**
     * Returns an empty summary result without calling the AI service.
     *
     * @param text News text to summarize (ignored)
     * @param method Summarization method (ignored)
     * @return [SummaryResult] with empty summary and disabled indicator:
     *   summary="", error="AI service disabled"
     */
    override fun summarize(text: String, method: String): SummaryResult {
        logger.debug { "NoOp: summarize called (AI service disabled), returning empty result" }
        return SummaryResult(
            summary = "",
            bullets = emptyList(),
            keywords = emptyList(),
            elapsedMs = null,
            error = "AI service disabled"
        )
    }
}
