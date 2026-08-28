package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.LikeRepository
import javax.inject.Inject

class ToggleLikeUseCase @Inject constructor(
    private val likeRepository: LikeRepository
) {
    suspend operator fun invoke(postId: String, userId: String): Result<Boolean> {
        return likeRepository.toggleLike(postId, userId)
    }
}