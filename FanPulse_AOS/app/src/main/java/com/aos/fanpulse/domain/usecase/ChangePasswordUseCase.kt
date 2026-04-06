package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.ChangePasswordRequest
import com.aos.fanpulse.data.remote.apiservice.MessageResponse
import com.aos.fanpulse.domain.repository.UserProfileRepository
import retrofit2.Response
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(
        currentPw: String,
        newPw: String,
        confirmPw: String
    ): Response<MessageResponse> {

        // 1. 빈 값 체크
        if (currentPw.isBlank() || newPw.isBlank()) {
            throw IllegalArgumentException("비밀번호를 모두 입력해주세요.")
        }

        // 2. 새 비밀번호 일치 확인
        if (newPw != confirmPw) {
            throw IllegalArgumentException("새 비밀번호가 일치하지 않습니다.")
        }

        // 3. 비밀번호 규칙 검사 (예: 8자 이상)
        if (newPw.length < 8) {
            throw IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.")
        }

        // 4. 모든 검증 통과 시 서버 전송
        return repository.changePassword(ChangePasswordRequest(currentPw, newPw))
    }
}