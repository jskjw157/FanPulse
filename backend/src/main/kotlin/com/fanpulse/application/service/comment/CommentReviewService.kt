package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.*
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentApprovedEvent
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.infrastructure.persistence.comment.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

/** Short claim/apply transactions. Never hold a database lock while waiting for AI. */
@Service
class CommentReviewService(
    private val comments: CommentJpaRepository,
    private val audits: CommentReviewAuditRepository,
    private val filter: CommentFilterPort,
    private val events: DomainEventPublisher,
    transactionManager: PlatformTransactionManager
) {
    private val tx = TransactionTemplate(transactionManager)

    fun mine(userId: UUID, postId: String, page: Int, size: Int): ReviewCommentPage = tx.execute {
        toPage(comments.findByPostIdAndUserIdAndStatusIn(
            postId, userId, listOf(CommentStatus.PENDING, CommentStatus.BLOCKED, CommentStatus.APPROVED),
            pageable(page, size)
        ))
    }!!

    fun pending(page: Int, size: Int): ReviewCommentPage = tx.execute {
        toPage(comments.findByStatus(CommentStatus.PENDING, pageable(page, size)))
    }!!

    fun history(id: UUID): List<CommentReviewAudit> = tx.execute {
        audits.findByCommentIdOrderByCreatedAtDesc(id, PageRequest.of(0, 100))
    }!!

    fun decide(id: UUID, actorId: UUID, request: CommentReviewDecision): ReviewCommentResponse = tx.execute {
        require(request.status == CommentStatus.APPROVED || request.status == CommentStatus.BLOCKED) {
            "Only APPROVED or BLOCKED decisions are allowed"
        }
        require(request.reason.isNotBlank() && request.reason.length <= 1000) { "A review reason is required (max 1000)" }
        val comment = comments.findByIdForUpdate(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found")
        if (comment.status != CommentStatus.PENDING || comment.version != request.expectedVersion) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Comment changed; refresh before deciding")
        }
        applyDecision(comment, request.status, request.reason, "ADMIN_${request.status}", actorId)
        ReviewCommentResponse.from(comment)
    }!!

    /** Returns attempts actually claimed, not candidates observed. Exhaustion stays Fail-Pending for an operator. */
    fun recheckPending(now: Instant = Instant.now()): Int {
        val ids = tx.execute { comments.findDueReviewIds(now, ReviewCommentResponse.MAX_ATTEMPTS, PageRequest.of(0, 20)) }!!
        var claimed = 0
        for (id in ids) {
            val claim = tx.execute {
                val comment = comments.findByIdForUpdate(id) ?: return@execute null
                if (comment.status != CommentStatus.PENDING || comment.reviewAttempts >= ReviewCommentResponse.MAX_ATTEMPTS ||
                    comment.nextReviewAt?.isAfter(now) == true) return@execute null
                comment.reviewAttempts++
                // Lease permits recovery after a process crash. A reclaimed row has a new version.
                comment.nextReviewAt = now.plusSeconds(300)
                comments.saveAndFlush(comment)
                audit(comment, "CLAIMED", "PENDING", "Automatic review claimed")
                Claim(comment.id, comment.version, comment.content)
            } ?: continue
            claimed++
            val result = try {
                filter.filterComment(claim.content)
            } catch (_: Exception) {
                FilterResult(isFiltered = false, filterType = "fallback", reason = "AI unavailable")
            }
            tx.executeWithoutResult {
                val comment = comments.findByIdForUpdate(claim.id)
                if (comment == null || comment.status != CommentStatus.PENDING ||
                    comment.version != claim.version || comment.content != claim.content) {
                    audits.save(CommentReviewAudit(
                        commentId = claim.id, action = "STALE_RESULT", toStatus = comment?.status?.name ?: "DELETED",
                        reason = "Comment changed after claim; result discarded", commentVersion = claim.version,
                        attempt = comment?.reviewAttempts ?: 0
                    ))
                    return@executeWithoutResult
                }
                when {
                    result.filterType.lowercase() in setOf("fallback", "noop") -> {
                        comment.nextReviewAt = if (comment.reviewAttempts >= ReviewCommentResponse.MAX_ATTEMPTS) null
                            else now.plusSeconds(60L * (1L shl (comment.reviewAttempts - 1)))
                        comments.saveAndFlush(comment)
                        audit(comment, "AI_UNAVAILABLE", "PENDING", "AI unavailable; manual review required after 5 attempts")
                    }
                    result.isFiltered -> applyDecision(
                        comment, CommentStatus.BLOCKED, result.reason?.takeIf { it.isNotBlank() } ?: "Blocked by AI",
                        "AI_BLOCKED"
                    )
                    else -> applyDecision(comment, CommentStatus.APPROVED, "AI review passed", "AI_APPROVED")
                }
            }
        }
        return claimed
    }

    private fun applyDecision(comment: Comment, status: CommentStatus, reason: String, action: String, actorId: UUID? = null) {
        if (status == CommentStatus.APPROVED) comment.approve() else comment.block(reason.take(1000))
        comment.nextReviewAt = null
        comments.saveAndFlush(comment) // Optimistic version predicate is the final CAS guard against other writers.
        audit(comment, action, status.name, reason.take(1000), actorId)
        if (status == CommentStatus.APPROVED) events.publish(CommentApprovedEvent(comment.id))
    }

    private fun audit(comment: Comment, action: String, toStatus: String, reason: String, actorId: UUID? = null) {
        audits.save(CommentReviewAudit(
            commentId = comment.id, actorId = actorId, action = action, toStatus = toStatus, reason = reason,
            commentVersion = comment.version, attempt = comment.reviewAttempts
        ))
    }

    private fun pageable(page: Int, size: Int) = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 100),
        Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")))

    private fun toPage(result: Page<Comment>) = ReviewCommentPage(
        result.content.map(ReviewCommentResponse::from), result.totalElements, result.number, result.size, result.totalPages
    )

    private data class Claim(val id: UUID, val version: Long, val content: String)
}
