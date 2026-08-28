package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.MyProfile
import com.aos.fanpulse.domain.repository.UserProfileRepository
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(): Result<MyProfile> {
        return try {
            val profile = userProfileRepository.getMyProfile()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}