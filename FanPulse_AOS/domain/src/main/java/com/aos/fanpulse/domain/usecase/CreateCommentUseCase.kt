package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.Comment
import com.aos.fanpulse.domain.model.CommentRequest
import com.aos.fanpulse.domain.repository.CommentsRepository
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val repository: CommentsRepository
) {
    suspend operator fun invoke(postId: String, content: String): Result<Comment> {

        return runCatching {
            val trimmedContent = content.trim()

            if (trimmedContent.isEmpty()) {
                throw IllegalArgumentException("댓글 내용을 입력해주세요.")
            }
            if (trimmedContent.length > 500) {
                throw IllegalArgumentException("댓글은 500자 이내로 작성해주세요.")
            }
            val request = CommentRequest(postId = postId, content = trimmedContent)
            repository.createComment(request)
        }
    }
}