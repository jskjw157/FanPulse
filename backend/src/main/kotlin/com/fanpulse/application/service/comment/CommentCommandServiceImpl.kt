package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentApprovedEvent
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.application.port.out.CommentFilterLogPort
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLog
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * AI 댓글 필터링 → 상태 결정 → 저장 → 로그 기록 → 메트릭 발행 파이프라인을 수행한다.
 *
 * @property commentFilterPort AI Sidecar 댓글 필터 포트 (Fail-Pending fallback 포함)
 * @property commentPort 댓글 영속성 포트
 * @property filterLogPort 필터 로그 영속성 포트
 * @property meterRegistry Micrometer 메트릭 레지스트리
 */
@Service
class CommentCommandServiceImpl(
    private val commentFilterPort: CommentFilterPort,
    private val commentPort: CommentPort,
    private val filterLogPort: CommentFilterLogPort,
    private val meterRegistry: MeterRegistry,
    private val posts: CommunityPostJpaRepository,
    private val eventPublisher: DomainEventPublisher
) : CommentCommandService {

    @Transactional
    override fun createComment(
        postId: String,
        userId: UUID,
        content: String,
        parentCommentId: UUID?
    ): CommentResponse {
        lockVisiblePost(postId)
        if (parentCommentId != null) {
            val parent = commentPort.findById(parentCommentId)
                ?: throw NoSuchElementException("부모 댓글을 찾을 수 없습니다")
            require(parent.postId == postId) { "부모 댓글은 같은 게시글에 속해야 합니다" }
            require(parent.status == CommentStatus.APPROVED) { "승인된 댓글에만 답글을 작성할 수 있습니다" }
        }

        // 1. 도메인 엔티티 생성 (content 검증 포함, PENDING 초기 상태)
        val comment = Comment.create(postId, userId, content, parentCommentId)
        return filterAndSave(comment)
    }

    @Transactional
    override fun updateComment(commentId: UUID, userId: UUID, content: String, postId: String?): CommentResponse {
        val comment = ownedComment(commentId, userId, postId)
        lockVisiblePost(comment.postId)
        if (comment.status == CommentStatus.DELETED) throw NoSuchElementException("댓글을 찾을 수 없습니다")
        comment.edit(content)
        return filterAndSave(comment)
    }

    @Transactional
    override fun deleteComment(commentId: UUID, userId: UUID, postId: String?) {
        val comment = ownedComment(commentId, userId, postId)
        lockVisiblePost(comment.postId)
        if (comment.status == CommentStatus.DELETED) return
        comment.delete()
        commentPort.save(comment)
    }

    private fun ownedComment(commentId: UUID, userId: UUID, postId: String?): Comment {
        val comment = commentPort.findById(commentId) ?: throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (postId != null && comment.postId != postId) throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (comment.userId != userId) throw AccessDeniedException("작성자만 댓글을 변경할 수 있습니다")
        return comment
    }

    private fun lockVisiblePost(postId: String) {
        // Preserve the mobile legacy ObjectId contract; UUID posts belong to community_posts.
        val id = runCatching { UUID.fromString(postId) }.getOrNull() ?: return
        posts.findPublishedByIdForUpdate(id) ?: throw NoSuchElementException("게시글을 찾을 수 없습니다")
    }

    private fun filterAndSave(comment: Comment): CommentResponse {

        // 2. AI 필터링 호출
        val filterResult = commentFilterPort.filterComment(comment.content)
        logger.debug { "Filter result for comment: $filterResult" }

        // 3. 상태 분기 (Fail-Pending 전략)
        resolveStatus(comment, filterResult)

        // 4. 댓글 저장 (save는 flush까지 수행해 @Version 충돌을 승인 이벤트보다 먼저 확정한다)
        val saved = commentPort.save(comment)

        // 5. 승인 이벤트는 flush 이후 같은 트랜잭션에서 동기 발행한다. AFTER_COMMIT으로 미루지 않는다.
        //    리스너 실패를 삼키지 않으므로 승인·감사·알림이 함께 롤백된다 (#299/#300/#302 공통 계약).
        if (saved.status == CommentStatus.APPROVED) {
            eventPublisher.publish(CommentApprovedEvent(saved.id))
        }

        // 6. 필터 감사 로그도 같은 트랜잭션(REQUIRED)에 참여한다. 실패를 숨기지 않는다.
        filterLogPort.save(CommentFilterLog.create(saved.id, filterResult))

        // 7. 메트릭 기록 (상태별 개별 카운터)
        val metricName = when (saved.status) {
            CommentStatus.APPROVED -> "comment.filter.approved"
            CommentStatus.BLOCKED -> "comment.filter.blocked"
            CommentStatus.PENDING -> "comment.filter.pending"
            CommentStatus.DELETED -> error("Deleted comments cannot be filtered")
        }
        meterRegistry.counter(metricName, "filter_type", filterResult.filterType).increment()

        return CommentResponse.from(saved)
    }

    private fun resolveStatus(comment: Comment, filterResult: FilterResult) {
        when {
            filterResult.isFiltered -> comment.block(filterResult.reason ?: "AI 필터에 의해 차단됨")
            filterResult.filterType.lowercase() in setOf("fallback", "noop") -> {
                // AI 장애 또는 명시적 비활성화 → PENDING 유지 (Fail-Pending)
            }
            else -> comment.approve()
        }
    }
}
