package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.application.port.out.CommentFilterLogPort
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentApprovedEvent
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.community.CommunityPostStatus
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLog
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * AI 댓글 필터링 → 상태 결정 → 저장 → 로그 기록 → 메트릭 발행 파이프라인을 수행한다.
 *
 * 원격 AI 호출은 트랜잭션 밖에서 수행하고, 저장 구간만 짧은 트랜잭션으로 묶는다.
 * 저장 트랜잭션은 게시글 row lock을 잡은 뒤 댓글의 현재 version/status/content를 다시 확인하므로
 * AI 대기 중 발생한 수정·삭제·운영자 결정을 덮어쓰지 않는다.
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
    private val eventPublisher: DomainEventPublisher,
    transactionManager: PlatformTransactionManager
) : CommentCommandService {

    private val writeTransaction = TransactionTemplate(transactionManager)

    override fun createComment(
        postId: String,
        userId: UUID,
        content: String,
        parentCommentId: UUID?
    ): CommentResponse {
        // 1. 권한·가시성·입력 검사는 원격 호출 전에 끝낸다.
        requireVisiblePost(postId)
        requireApprovedParent(postId, parentCommentId)
        val comment = Comment.create(postId, userId, content, parentCommentId)

        // 2. AI 필터링은 트랜잭션/락 없이 수행한다.
        val filterResult = filterComment(comment.content)

        // 3. 저장 구간만 짧은 트랜잭션으로 처리하고, 락 안에서 다시 검증한다.
        return writeTransaction.execute {
            lockVisiblePost(postId)
            requireApprovedParent(postId, parentCommentId)
            val saved = persist(comment, filterResult)
            CommentResponse.from(saved)
        } ?: error("Comment transaction returned no result")
    }

    override fun updateComment(commentId: UUID, userId: UUID, content: String, postId: String?): CommentResponse {
        // 1. 현재 스냅샷으로 권한·상태·version을 확인하고 입력을 검증한다.
        val snapshot = commentPort.findById(commentId)
            ?: throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (postId != null && snapshot.postId != postId) throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (snapshot.userId != userId) throw AccessDeniedException("작성자만 댓글을 변경할 수 있습니다")
        if (snapshot.status == CommentStatus.DELETED) throw NoSuchElementException("댓글을 찾을 수 없습니다")
        val normalized = content.trim()
        require(normalized.isNotBlank()) { "Comment content cannot be blank" }
        val expectedVersion = snapshot.version
        val expectedPostId = snapshot.postId

        // 2. AI 필터링은 트랜잭션/락 없이 수행한다. edit()가 저장하는 값과 같은 정규화본을 넘긴다.
        val filterResult = filterComment(normalized)

        // 3. 저장 구간에서 락을 잡고 현재 값이 스냅샷과 같은지 다시 확인한다.
        return writeTransaction.execute {
            // create/delete와 같은 순서(게시글 → 댓글)로 잠가 교착을 피한다.
            lockVisiblePost(expectedPostId)
            val comment = commentPort.findByIdForUpdate(commentId)
                ?: throw NoSuchElementException("댓글을 찾을 수 없습니다")
            if (comment.postId != expectedPostId) throw NoSuchElementException("댓글을 찾을 수 없습니다")
            if (comment.userId != userId) throw AccessDeniedException("작성자만 댓글을 변경할 수 있습니다")
            if (comment.status == CommentStatus.DELETED) throw NoSuchElementException("댓글을 찾을 수 없습니다")
            if (comment.version != expectedVersion) throw staleComment()
            comment.edit(normalized)
            val saved = persist(comment, filterResult)
            CommentResponse.from(saved)
        } ?: error("Comment transaction returned no result")
    }

    @Transactional
    override fun deleteComment(commentId: UUID, userId: UUID, postId: String?) {
        val snapshot = commentPort.findById(commentId)
            ?: throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (postId != null && snapshot.postId != postId) throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (snapshot.userId != userId) throw AccessDeniedException("작성자만 댓글을 변경할 수 있습니다")
        // create/update와 같은 순서(게시글 → 댓글)로 잠근다.
        lockVisiblePost(snapshot.postId)
        val comment = commentPort.findByIdForUpdate(commentId)
            ?: throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (comment.postId != snapshot.postId) throw NoSuchElementException("댓글을 찾을 수 없습니다")
        if (comment.userId != userId) throw AccessDeniedException("작성자만 댓글을 변경할 수 있습니다")
        if (comment.status == CommentStatus.DELETED) return
        comment.delete()
        commentPort.save(comment)
    }

    private fun filterComment(content: String): FilterResult {
        val filterResult = commentFilterPort.filterComment(content)
        logger.debug { "Filter result for comment: $filterResult" }
        return filterResult
    }

    /**
     * 호출자가 연 트랜잭션이 없을 때만 동작하는 짧은 쓰기 구간.
     * 승인 이벤트·감사 로그·메트릭은 저장과 같은 트랜잭션에 참여한다.
     */
    private fun persist(comment: Comment, filterResult: FilterResult): Comment {
        resolveStatus(comment, filterResult)
        // save는 flush까지 수행해 @Version 충돌을 승인 이벤트보다 먼저 확정한다.
        val saved = commentPort.save(comment)

        // 승인 이벤트는 flush 이후 같은 트랜잭션에서 동기 발행한다. AFTER_COMMIT으로 미루지 않는다.
        if (saved.status == CommentStatus.APPROVED) {
            eventPublisher.publish(CommentApprovedEvent(saved.id))
        }

        // 필터 감사 로그도 같은 트랜잭션(REQUIRED)에 참여한다. 실패를 숨기지 않는다.
        filterLogPort.save(CommentFilterLog.create(saved.id, filterResult))

        val metricName = when (saved.status) {
            CommentStatus.APPROVED -> "comment.filter.approved"
            CommentStatus.BLOCKED -> "comment.filter.blocked"
            CommentStatus.PENDING -> "comment.filter.pending"
            CommentStatus.DELETED -> error("Deleted comments cannot be filtered")
        }
        meterRegistry.counter(metricName, "filter_type", filterResult.filterType).increment()
        return saved
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

    /** 원격 호출 전 가시성 검사. UUID 게시글만 community_posts에서 확인한다. */
    private fun requireVisiblePost(postId: String) {
        val id = runCatching { UUID.fromString(postId) }.getOrNull() ?: return
        val post = posts.findById(id).orElse(null)
            ?: throw NoSuchElementException("게시글을 찾을 수 없습니다")
        if (post.status != CommunityPostStatus.PUBLISHED) throw NoSuchElementException("게시글을 찾을 수 없습니다")
    }

    private fun requireApprovedParent(postId: String, parentCommentId: UUID?) {
        if (parentCommentId == null) return
        val parent = commentPort.findById(parentCommentId)
            ?: throw NoSuchElementException("부모 댓글을 찾을 수 없습니다")
        require(parent.postId == postId) { "부모 댓글은 같은 게시글에 속해야 합니다" }
        require(parent.status == CommentStatus.APPROVED) { "승인된 댓글에만 답글을 작성할 수 있습니다" }
    }

    private fun lockVisiblePost(postId: String) {
        // Preserve the mobile legacy ObjectId contract; UUID posts belong to community_posts.
        val id = runCatching { UUID.fromString(postId) }.getOrNull() ?: return
        posts.findPublishedByIdForUpdate(id) ?: throw NoSuchElementException("게시글을 찾을 수 없습니다")
    }

    private fun staleComment() = ResponseStatusException(
        HttpStatus.CONFLICT,
        "Comment changed while moderating; refresh before editing"
    )
}
