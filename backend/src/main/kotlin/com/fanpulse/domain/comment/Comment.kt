package com.fanpulse.domain.comment

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "comments")
class Comment private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "post_id", length = 24, nullable = false)
    val postId: String,

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    val userId: UUID,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var status: CommentStatus,

    @Column(name = "parent_comment_id", columnDefinition = "uuid")
    val parentCommentId: UUID?,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant
) {
    @Column(name = "block_reason", columnDefinition = "TEXT")
    var blockReason: String? = null
        private set

    companion object {
        fun create(
            postId: String,
            userId: UUID,
            content: String,
            parentCommentId: UUID? = null
        ): Comment {
            require(content.isNotBlank()) { "Comment content cannot be blank" }

            val now = Instant.now()
            return Comment(
                id = UUID.randomUUID(),
                postId = postId,
                userId = userId,
                content = content,
                status = CommentStatus.PENDING,
                parentCommentId = parentCommentId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun approve() {
        this.status = CommentStatus.APPROVED
        this.updatedAt = Instant.now()
    }

    fun block(reason: String) {
        require(reason.isNotBlank()) { "Block reason cannot be blank" }
        this.status = CommentStatus.BLOCKED
        this.blockReason = reason
        this.updatedAt = Instant.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Comment) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
