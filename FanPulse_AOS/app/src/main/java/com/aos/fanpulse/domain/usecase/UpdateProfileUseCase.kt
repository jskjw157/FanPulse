package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.MyProfile
import com.aos.fanpulse.data.remote.apiservice.UpdateProfileRequest
import com.aos.fanpulse.domain.repository.UserProfileRepository
import retrofit2.Response
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    /**
     * @param nickname 변경할 닉네임
     * @param bio 변경할 자기소개 (선택 사항)
     */
    suspend operator fun invoke(
        nickname: String,
        bio: String? = null
    ): Response<MyProfile> {

        // 닉네임 유효성 검사
        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isEmpty()) {
            throw IllegalArgumentException("닉네임은 비워둘 수 없습니다.")
        }
        if (trimmedNickname.length > 20) {
            throw IllegalArgumentException("닉네임은 최대 20자까지 가능합니다.")
        }

        // Request 객체 생성 및 Repository 호출
        val request = UpdateProfileRequest(trimmedNickname)

        return repository.updateProfile(request)
    }
}