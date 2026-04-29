package com.aos.fanpulse.data.remote

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject

class GoogleSignInDataSourceImpl @Inject constructor(
    private val credentialManager: CredentialManager,
    private val googleIdOption: GetGoogleIdOption,
) : GoogleSignInDataSource {
    //  구글 통신 로직
    override suspend fun signIn(activityContext: Context): Result<String> {
        return try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = response.credential
            //  type	어떤 종류의 인증인지 알려주는 문자열입니다. 구글 로그인의 경우 com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL 값이 들어옵니다.
            //  data    가장 중요한 부분입니다. 실제 토큰 정보가 담긴 Bundle 객체입니다. 암호화된 데이터 덩어리라고 보시면 됩니다.
            //  id      (선택 사항) 사용자의 이메일이나 고유 식별자가 직접 들어오는 경우도 있지만, 보안상 보통은 data를 통해 추출합니다.
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                Result.success(idToken)
            } else {
                Result.failure(Exception("예상치 못한 인증 유형입니다: ${credential.type}"))
            }

        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
