package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.domain.ai.FilterResult
import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * JPA entity for AI comment filter audit logs.
 * 댓글 AI 필터링 결과를 기록하는 감사 로그 엔티티.
 *
 * @property commentId 필터링 대상 댓글 ID
 * @property isFiltered AI가 유해하다고 판단했는지 여부
 * @property filterType 필터 유형 (ai / fallback 등)
 * @property reason 차단 사유 (nullable)
 * @property ruleName 적용된 규칙명 (nullable)
 */
@Entity
@Table(name = "comment_filter_logs")
class CommentFilterLog private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "comment_id", columnDefinition = "uuid", nullable = false)
    val commentId: UUID,

    @Column(name = "is_filtered", nullable = false)
    val isFiltered: Boolean,

    @Column(name = "filter_type", length = 50, nullable = false)
    val filterType: String,

    @Column(columnDefinition = "TEXT")
    val reason: String?,

    @Column(name = "rule_name", length = 100)
    val ruleName: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant
) {
    companion object {
        fun create(commentId: UUID, filterResult: FilterResult): CommentFilterLog {
            return CommentFilterLog(
                id = UUID.randomUUID(),
                commentId = commentId,
                isFiltered = filterResult.isFiltered,
                filterType = filterResult.filterType,
                reason = filterResult.reason,
                ruleName = filterResult.ruleName,
                createdAt = Instant.now()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommentFilterLog) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
