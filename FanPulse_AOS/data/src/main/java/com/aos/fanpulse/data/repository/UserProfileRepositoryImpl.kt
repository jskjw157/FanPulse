package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toData
import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.domain.model.ChangePasswordRequest
import com.aos.fanpulse.domain.model.MessageResponse
import com.aos.fanpulse.domain.model.MyProfile
import com.aos.fanpulse.domain.model.UpdateProfileRequest
import com.aos.fanpulse.domain.model.UpdateSettingsRequest
import com.aos.fanpulse.data.remote.apiservice.UserProfileApiService
import com.aos.fanpulse.domain.model.UserSettings
import com.aos.fanpulse.domain.repository.UserProfileRepository
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val apiService: UserProfileApiService
) : UserProfileRepository {
    override suspend fun getMyProfile(): MyProfile {
        val response = apiService.getMyProfile()
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("프로필 정보를 불러올 수 없습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): MyProfile {
        // 1. 도메인 요청 모델을 DTO로 변환하여 전송
        val response = apiService.updateProfile(request.toData())

        if (response.isSuccessful) {
            // 2. 수정된 결과 DTO를 다시 도메인 모델로 변환하여 반환
            return response.body()?.toDomain() ?: throw Exception("프로필 수정에 실패했습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }

    override suspend fun getMySettings(): UserSettings {
        val response = apiService.getMySettings()
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("설정 정보를 불러올 수 없습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }

    override suspend fun updateSettings(request: UpdateSettingsRequest): UserSettings {
        val response = apiService.updateSettings(request.toData())
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("설정 저장에 실패했습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequest): MessageResponse {
        val response = apiService.changePassword(request.toData())
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("비밀번호 변경 응답이 올바르지 않습니다.")
        } else {
            throw Exception("비밀번호 변경 실패: ${response.code()}")
        }
    }
}