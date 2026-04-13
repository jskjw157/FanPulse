package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.Comment
import com.aos.fanpulse.data.remote.apiservice.CommentListResponse
import com.aos.fanpulse.data.remote.apiservice.CommentRequest
import retrofit2.Response

interface CommentsRepository {
    /**
     * 특정 게시글의 댓글 목록 조회
     */
    suspend fun getComments(
        postId: String,
        page: Int = 0,
        size: Int = 20
    ): Response<CommentListResponse>

    /**
     * 새 댓글 또는 답글 작성
     */
    suspend fun createComment(
        request: CommentRequest
    ): Response<Comment>
}