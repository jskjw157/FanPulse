package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.PostRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: String, currentUserId: String): Result<Unit> {
        return repository.deletePost(postId, currentUserId)
    }
}