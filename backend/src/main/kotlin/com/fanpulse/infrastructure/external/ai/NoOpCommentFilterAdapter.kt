package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * No-Op implementation of [CommentFilterPort] used when the AI service is disabled.
 *
 * Active only when `fanpulse.ai-service.enabled=false`.
 * When the AI service feature flag is turned off, this bean replaces [AiCommentFilterAdapter]
 * and returns permissive (Fail-Open) results without making any HTTP calls.
 *
 * Fail-Open strategy:
 * - [filterComment] → isFiltered=false, filterType="noop"
 *
 * @see AiCommentFilterAdapter for the real implementation
 * @see CommentFilterPort for the port contract
 */
@Component
@ConditionalOnProperty(name = ["fanpulse.ai-service.enabled"], havingValue = "false")
class NoOpCommentFilterAdapter : CommentFilterPort {

    /**
     * Returns a permissive filter result without calling the AI service.
     *
     * @param content Comment content to filter (ignored)
     * @return [FilterResult] with Fail-Open permissive values:
     *   isFiltered=false, filterType="noop"
     */
    override fun filterComment(content: String): FilterResult {
        logger.debug { "NoOp: filterComment called (AI service disabled), returning permissive result" }
        return FilterResult(
            isFiltered = false,
            filterType = "noop",
            reason = null,
            ruleName = null
        )
    }
}
