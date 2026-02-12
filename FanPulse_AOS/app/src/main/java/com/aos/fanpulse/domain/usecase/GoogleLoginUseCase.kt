package com.aos.fanpulse.domain.usecase

import android.content.Context
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import com.aos.fanpulse.domain.repository.UserDataRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val googleSignInRepository: GoogleSignInRepository,
    private val userDataRepository: UserDataRepository
) {
    suspend operator fun invoke(activityContext: Context): Result<Unit> {
        val result = googleSignInRepository.signIn(activityContext)

        return result.mapCatching { credential ->
            val idToken = when (credential.type) {
                TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    googleIdTokenCredential.idToken
                }
                else -> throw Exception("지원하지 않는 인증 타입입니다.")
            }
            // Id 토큰 저장
            userDataRepository.updateIdToken(idToken)
        }
    }
    /**
     *  Result.success(Unit)
     *  Result.failure(Exception)
     *  Result.failure(IOException)
     * */
}