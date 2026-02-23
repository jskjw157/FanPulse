package com.aos.fanpulse.domain.usecase

import android.content.Context
import com.aos.fanpulse.data.remote.AuthApiService
import com.aos.fanpulse.data.remote.GoogleLoginRequest
import com.aos.fanpulse.data.remote.User
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import com.aos.fanpulse.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val googleSignInRepository: GoogleSignInRepository,
    private val authRepository: AuthRepository,
    private val authApiService: AuthApiService,
) {
    suspend operator fun invoke(activityContext: Context): Result<User> {
        return runCatching {
            val credential = googleSignInRepository.signIn(activityContext).getOrThrow()

            val googleIdToken = when (credential.type) {
                TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                }
                else -> throw Exception("Unsupported type")
            }

            // 서버에 ID Token 전송 (이때 서버가 응답 헤더로 쿠키를 내려줍니다)
            val response = authApiService.loginWithGoogle(GoogleLoginRequest(googleIdToken))

            if (response.isSuccessful) {
                response.body()?.user ?: throw Exception("유저 정보가 없습니다.")
            } else {
                throw Exception("서버 로그인 실패: ${response.code()}")
            }
        }
    }
    /**
     *  Result.success(Unit)
     *  Result.failure(Exception)
     *  Result.failure(IOException)
     * */
}