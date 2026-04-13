package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.remote.apiservice.Comment
import com.aos.fanpulse.data.remote.apiservice.CommentListResponse
import com.aos.fanpulse.data.remote.apiservice.CommentRequest
import com.aos.fanpulse.data.remote.apiservice.CommentsApiService
import com.aos.fanpulse.domain.repository.CommentsRepository
import retrofit2.Response
import javax.inject.Inject

class CommentsRepositoryImpl @Inject constructor(
    private val apiService: CommentsApiService
) : CommentsRepository {
    /**
     * 특정 게시글의 댓글 목록 조회
     * @param postId 대상 게시글 ID
     * @param page 페이지 번호 (기본값 0)
     * @param size 한 페이지당 개수 (기본값 20)
     */
    override suspend fun getComments(
        postId: String,
        page: Int,
        size: Int
    ): Response<CommentListResponse> {
        return apiService.getComments(
            postId = postId,
            page = page,
            size = size
        )
    }

    /**
     * 새 댓글 또는 답글 작성
     * @param request 댓글 작성에 필요한 데이터 (게시글 ID, 내용 등)
     */
    override suspend fun createComment(
        request: CommentRequest
    ): Response<Comment> {
        return apiService.createComment(request)
    }
}
