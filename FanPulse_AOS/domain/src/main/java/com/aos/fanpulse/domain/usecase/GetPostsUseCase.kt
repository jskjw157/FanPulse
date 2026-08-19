package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.domain.repository.PostRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(artistCategory: String?, currentUserId: String): Result<List<Post>> {
        return postRepository.fetchPosts(artistCategory, currentUserId)
    }
}