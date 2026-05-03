package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.Comment
import com.aos.fanpulse.data.remote.dto.CommentListResponse
import com.aos.fanpulse.data.remote.dto.CommentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CommentsApiService {

    /**
     * 특정 게시글의 댓글 목록 조회 (페이징)
     * @param postId 대상 게시글 ID
     */
    @GET("comments")
    suspend fun getComments(
        @Query("postId") postId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<CommentListResponse>

    /**
     * 새 댓글 또는 답글 작성
     * @param request 댓글 작성에 필요한 데이터
     */
    @POST("comments")
    suspend fun createComment(
        @Body request: CommentRequest
    ): Response<Comment>

}
