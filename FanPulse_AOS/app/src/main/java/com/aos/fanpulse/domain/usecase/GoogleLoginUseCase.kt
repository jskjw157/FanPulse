package com.aos.fanpulse.domain.usecase

import android.content.Context
import com.aos.fanpulse.data.remote.AuthApiService
import com.aos.fanpulse.data.remote.GoogleLoginRequest
import com.aos.fanpulse.domain.repository.AuthRepository
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val googleSignInRepository: GoogleSignInRepository,
    private val authApiService: AuthApiService,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(activityContext: Context): Result<Unit> {
        return runCatching {
            // 1. 구글 인증 크리덴셜 획득
            val credential = googleSignInRepository.signIn(activityContext).getOrThrow()
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

            // 2. 서버 로그인 요청 (구글 ID 토큰 전달)
            val response = authApiService.loginWithGoogle(GoogleLoginRequest(googleIdToken))

            if (response.isSuccessful) {
                // 3. 응답 헤더에서 Set-Cookie 값들 가져오기
                val cookies = response.headers().values("Set-Cookie")

                // 4. 쿠키 문자열에서 각 토큰 값 추출
                val access = extractToken(cookies, "fanpulse_access_token")
                val refresh = extractToken(cookies, "fanpulse_refresh_token")

                if (access != null && refresh != null) {
                    // 5. AuthRepository를 통해 DataStore에 저장
                    authRepository.updateTokens(access, refresh)
                    Unit
                } else {
                    throw Exception("응답 헤더에 토큰이 누락되었습니다.")
                }
            } else {
                throw Exception("서버 인증 실패: ${response.code()}")
            }
        }
    }

    private fun extractToken(cookies: List<String>, key: String): String? {
        return cookies.find { it.contains(key) }
            ?.substringAfter("$key=")
            ?.substringBefore(";")
            ?.trim()
    }
}