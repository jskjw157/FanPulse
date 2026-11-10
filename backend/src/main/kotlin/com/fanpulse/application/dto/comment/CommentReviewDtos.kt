package com.fanpulse.application.dto.comment

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class ReviewCommentResponse(
    val id: UUID,
    val postId: String,
    val userId: UUID,
    val content: String,
    val status: CommentStatus,
    val parentCommentId: UUID?,
    val createdAt: Instant,
    val authorName: String? = null,
    val version: Long,
    val blockReason: String?,
    val reviewAttempts: Int,
    val nextReviewAt: Instant?,
    val requiresManualReview: Boolean
) {
    companion object {
        const val MAX_ATTEMPTS = 5
        fun from(comment: Comment) = ReviewCommentResponse(
            comment.id, comment.postId, comment.userId, comment.content, comment.status,
            comment.parentCommentId, comment.createdAt, version = comment.version,
            blockReason = comment.blockReason, reviewAttempts = comment.reviewAttempts,
            nextReviewAt = comment.nextReviewAt,
            requiresManualReview = comment.status == CommentStatus.PENDING && comment.reviewAttempts >= MAX_ATTEMPTS
        )
    }
}

data class ReviewCommentPage(
    val content: List<ReviewCommentResponse>, val totalElements: Long,
    val page: Int, val size: Int, val totalPages: Int
)

data class CommentReviewDecision(
    @field:Min(0) val expectedVersion: Long,
    val status: CommentStatus,
    @field:NotBlank @field:Size(max = 1000) val reason: String
)
