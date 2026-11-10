package com.fanpulse.infrastructure.persistence.comment

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "comment_review_audits")
class CommentReviewAudit(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "comment_id", nullable = false) val commentId: UUID,
    @Column(name = "actor_id") val actorId: UUID? = null,
    @Column(nullable = false, length = 40) val action: String,
    @Column(name = "from_status", nullable = false, length = 20) val fromStatus: String = "PENDING",
    @Column(name = "to_status", nullable = false, length = 20) val toStatus: String,
    @Column(nullable = false, length = 1000) val reason: String,
    @Column(name = "comment_version", nullable = false) val commentVersion: Long,
    @Column(nullable = false) val attempt: Int,
    @Column(name = "created_at", nullable = false) val createdAt: Instant = Instant.now()
)

interface CommentReviewAuditRepository : JpaRepository<CommentReviewAudit, UUID> {
    fun findByCommentIdOrderByCreatedAtDesc(commentId: UUID, pageable: Pageable): List<CommentReviewAudit>
}
