package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toData
import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.domain.model.Comment
import com.aos.fanpulse.domain.model.CommentListResponse
import com.aos.fanpulse.domain.model.CommentRequest
import com.aos.fanpulse.data.remote.apiservice.CommentsApiService
import com.aos.fanpulse.domain.repository.CommentsRepository
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
    ): CommentListResponse {
        val response = apiService.getComments(
            postId = postId,
            page = page,
            size = size
        )

        if (response.isSuccessful) {
            // CommentListResponse DTO를 Domain 모델로 변환
            return response.body()?.toDomain() ?: throw Exception("Empty Comment List")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 새 댓글 또는 답글 작성
     * @param request 댓글 작성에 필요한 데이터 (게시글 ID, 내용 등)
     */
    override suspend fun createComment(
        request: CommentRequest
    ): Comment {
        // 1. 도메인 모델인 request를 서버 DTO인 toData()로 변환해서 전송
        val response = apiService.createComment(request.toData())

        if (response.isSuccessful) {
            // 2. 서버에서 받은 Comment DTO를 도메인 모델로 변환해서 반환
            return response.body()?.toDomain() ?: throw Exception("Comment Creation Failed")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }
}
