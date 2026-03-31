package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentResponse
import java.util.*

/**
 * AI 필터링을 포함한 댓글 생성/수정 작업을 정의한다.
 */
interface CommentCommandService {

    /**
     * AI 콘텐츠 필터링을 적용하여 새 댓글을 생성한다.
     * Fail-Pending 전략: AI 필터 장애 시 PENDING 상태로 저장한다.
     *
     * @param postId target post identifier
     * @param userId author's user ID
     * @param content comment text content
     * @param parentCommentId parent comment ID for replies (null for top-level)
     * @return created comment with resolved status (APPROVED / BLOCKED / PENDING)
     */
    fun createComment(postId: String, userId: UUID, content: String, parentCommentId: UUID? = null): CommentResponse
}
