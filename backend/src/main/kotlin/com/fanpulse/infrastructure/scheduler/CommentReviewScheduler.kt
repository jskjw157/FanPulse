package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.comment.CommentReviewService
import mu.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["fanpulse.comment-review.enabled"], havingValue = "true", matchIfMissing = true)
class CommentReviewScheduler(private val reviews: CommentReviewService) {
    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedDelayString = "\${fanpulse.comment-review.interval-ms:60000}")
    @SchedulerLock(name = "commentReviewRecovery", lockAtMostFor = "5m", lockAtLeastFor = "5s")
    fun recheck() {
        try {
            reviews.recheckPending()
        } catch (e: Exception) {
            // Claimed leases expire; no terminal decision or audit is partially committed.
            logger.error(e) { "Comment review recovery failed; pending claims will be retried after lease expiry" }
        }
    }
}
