package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.Comment
import com.aos.fanpulse.data.remote.apiservice.CommentRequest
import com.aos.fanpulse.domain.repository.CommentsRepository
import retrofit2.Response
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val repository: CommentsRepository
) {
    suspend operator fun invoke(postId: String, content: String): Response<Comment> {
        val trimmedContent = content.trim()

        // 1. 유효성 검사
        if (trimmedContent.isEmpty()) {
            throw IllegalArgumentException("댓글 내용을 입력해주세요.")
        }
        if (trimmedContent.length > 500) {
            throw IllegalArgumentException("댓글은 500자 이내로 작성해주세요.")
        }

        // 2. Request 객체 생성 및 호출
        val request = CommentRequest(postId = postId, content = trimmedContent)
        return repository.createComment(request)
    }
}