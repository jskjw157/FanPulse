package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.Comment
import com.aos.fanpulse.domain.model.CommentListResponse
import com.aos.fanpulse.domain.model.CommentRequest

interface CommentsRepository {
    /**
     * 특정 게시글의 댓글 목록 조회
     */
    suspend fun getComments(
        postId: String,
        page: Int = 0,
        size: Int = 20
    ): CommentListResponse

    /**
     * 새 댓글 또는 답글 작성
     */
    suspend fun createComment(
        request: CommentRequest
    ): Comment
}