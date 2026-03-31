package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentListResponse
import org.springframework.data.domain.Pageable

/**
 * 승인된(APPROVED) 댓글만 조회하는 읽기 전용 서비스를 정의한다.
 */
interface CommentQueryService {

    /**
     * 게시글의 승인된 댓글을 페이지네이션하여 조회한다.
     * APPROVED 상태 댓글만 반환한다 (BLOCKED, PENDING 제외).
     *
     * @param postId target post identifier
     * @param pageable pagination parameters
     */
    fun getComments(postId: String, pageable: Pageable): CommentListResponse
}
