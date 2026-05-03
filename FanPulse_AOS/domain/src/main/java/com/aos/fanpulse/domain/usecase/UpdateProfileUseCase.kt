package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.MyProfile
import com.aos.fanpulse.domain.model.UpdateProfileRequest
import com.aos.fanpulse.domain.repository.UserProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    /**
     * @param nickname 변경할 닉네임
     * @param bio 변경할 자기소개 (선택 사항)
     */
    suspend operator fun invoke(nickname: String, bio: String? = null): Result<MyProfile> = runCatching {
        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isEmpty()) throw IllegalArgumentException("닉네임은 필수입니다.")

        repository.updateProfile(UpdateProfileRequest(username = trimmedNickname))
    }
}