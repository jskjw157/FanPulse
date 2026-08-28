package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.domain.repository.PostRepository
import javax.inject.Inject

class GetPostDetailUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, currentUserId: String): Result<Post> {
        if (postId.isBlank()) {
            return Result.failure(IllegalArgumentException("잘못된 게시글 ID입니다."))
        }

        return postRepository.fetchPostDetail(postId, currentUserId)
    }
}