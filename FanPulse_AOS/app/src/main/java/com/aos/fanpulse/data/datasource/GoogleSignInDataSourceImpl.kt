package com.aos.fanpulse.data.datasource

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleSignInDataSourceImpl @Inject constructor(
    private val credentialManager: CredentialManager,
    private val googleIdOption: GetGoogleIdOption,
    @ApplicationContext private val context: Context
) : GoogleSignInDataSource {

    override suspend fun signIn(): Result<Credential> {
        return try {
            // Credential 요청 생성
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption) // 주입된 옵션 사용
                .build()

            // CredentialManager로 요청 실행
            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            // 응답에서 Credential 추출
            val credential = response.credential

            Result.success(credential)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        }
    }
}