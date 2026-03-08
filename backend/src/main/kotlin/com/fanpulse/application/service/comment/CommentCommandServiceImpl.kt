package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLog
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLogAdapter
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class CommentCommandServiceImpl(
    private val commentFilterPort: CommentFilterPort,
    private val commentPort: CommentPort,
    private val filterLogAdapter: CommentFilterLogAdapter,
    private val meterRegistry: MeterRegistry
) : CommentCommandService {

    @Transactional
    override fun createComment(
        postId: String,
        userId: UUID,
        content: String,
        parentCommentId: UUID?
    ): CommentResponse {
        // 1. 도메인 엔티티 생성 (content 검증 포함, PENDING 초기 상태)
        val comment = Comment.create(postId, userId, content, parentCommentId)

        // 2. AI 필터링 호출
        val filterResult = commentFilterPort.filterComment(content)
        logger.debug { "Filter result for comment: $filterResult" }

        // 3. 상태 분기 (Fail-Pending 전략)
        resolveStatus(comment, filterResult)

        // 4. 댓글 저장
        val saved = commentPort.save(comment)

        // 5. 필터 로그 저장 (runCatching으로 격리 — 실패해도 댓글 저장에 영향 없음)
        runCatching {
            val log = CommentFilterLog.create(saved.id, filterResult)
            filterLogAdapter.save(log)
        }.onFailure { e ->
            logger.warn(e) { "Failed to save filter log for comment ${saved.id}" }
        }

        // 6. 메트릭 기록
        meterRegistry.counter(
            "comment.created",
            "status", saved.status.name,
            "filter_type", filterResult.filterType
        ).increment()

        return CommentResponse.from(saved)
    }

    private fun resolveStatus(comment: Comment, filterResult: FilterResult) {
        when {
            filterResult.isFiltered -> comment.block(filterResult.reason ?: "AI 필터에 의해 차단됨")
            filterResult.filterType == "fallback" -> {} // AI 장애 → PENDING 유지 (Fail-Pending)
            else -> comment.approve()
        }
    }
}
