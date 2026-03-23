package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.ModerationResult
import com.fanpulse.domain.ai.port.ContentModerationPort
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * No-Op implementation of [ContentModerationPort] used when the AI service is disabled.
 *
 * Active only when `fanpulse.ai-service.enabled=false`.
 * When the AI service feature flag is turned off, this bean replaces [AiModerationAdapter]
 * and returns permissive (Fail-Open) results without making any HTTP calls.
 *
 * Fail-Open strategy:
 * - [checkContent] → isFlagged=false, action="allow"
 * - [batchCheck] → list of permissive results
 *
 * @see AiModerationAdapter for the real implementation
 * @see ContentModerationPort for the port contract
 */
@Component
@ConditionalOnProperty(name = ["fanpulse.ai-service.enabled"], havingValue = "false")
class NoOpContentModerationAdapter : ContentModerationPort {

    /**
     * Returns a permissive moderation result without calling the AI service.
     *
     * @param text Text content to check (ignored)
     * @return [ModerationResult] with Fail-Open permissive values:
     *   isFlagged=false, action="allow", modelUsed="noop"
     */
    override fun checkContent(text: String): ModerationResult {
        logger.debug { "NoOp: checkContent called (AI service disabled), returning permissive result" }
        return ModerationResult(
            isFlagged = false,
            action = "allow",
            highestCategory = null,
            highestScore = null,
            confidence = 0.0,
            modelUsed = "noop",
            processingTimeMs = null,
            error = null
        )
    }

    /**
     * Returns a list of permissive moderation results without calling the AI service.
     *
     * @param texts List of text contents to check (ignored)
     * @return List of [ModerationResult] with Fail-Open permissive values
     */
    override fun batchCheck(texts: List<String>): List<ModerationResult> {
        logger.debug { "NoOp: batchCheck called for ${texts.size} texts (AI service disabled), returning permissive results" }
        return texts.map {
            ModerationResult(
                isFlagged = false,
                action = "allow",
                highestCategory = null,
                highestScore = null,
                confidence = 0.0,
                modelUsed = "noop",
                processingTimeMs = null,
                error = null
            )
        }
    }
}
