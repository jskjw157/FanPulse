package com.aos.fanpulse.data.repository

import androidx.datastore.core.DataStore
import com.aos.fanpulse.data.remote.apiservice.AuthenticationApiService
import com.aos.fanpulse.data.remote.dto.GoogleLoginRequest
import com.aos.fanpulse.datastore.UserData
import com.aos.fanpulse.domain.model.AuthToken
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * 데이터의 입구와 출구 역할
 * 데이터를 어떻게 가져오고 보낼 것인가?
 * */
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthenticationApiService,
    private val userDataStore: DataStore<UserData>,
    private val firebaseAuth: FirebaseAuth,
) : AuthenticationRepository {

    override val authTokens: Flow<AuthToken> = userDataStore.data
        .map { userData ->
            AuthToken(
                accessToken = userData.accessToken.ifEmpty { null },
                refreshToken = userData.refreshToken.ifEmpty { null }
            )
        }

    override suspend fun updateTokens(access: String, refresh: String) {
        userDataStore.updateData { currentData ->
            currentData.toBuilder()
                .setAccessToken(access)
                .setRefreshToken(refresh)
                .build()
        }
    }

    override suspend fun clearAll() {
        userDataStore.updateData { currentData ->
            currentData.toBuilder()
                .clearAccessToken()
                .clearRefreshToken()
                .build()
        }
    }

    override suspend fun loginWithGoogle(googleIdToken: String): Result<AuthToken> = runCatching {
        val response = authApiService.loginWithGoogle(GoogleLoginRequest(googleIdToken))
        if (response.isSuccessful) {
            val cookies = response.headers().values("Set-Cookie")
            val access = extractToken(cookies, "fanpulse_access_token")
            val refresh = extractToken(cookies, "fanpulse_refresh_token")

            if (access != null && refresh != null) {

                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                firebaseAuth.signInWithCredential(credential).await()

                updateTokens(access, refresh)
                AuthToken(accessToken = access, refreshToken = refresh)
            } else {
                throw Exception("토큰 정보가 응답 헤더에 없습니다.")
            }
        } else {
            throw Exception("서버 로그인 실패: ${response.code()}")
        }
    }
    private fun extractToken(cookies: List<String>, key: String): String? {
        return cookies.find { it.contains(key) }
            ?.substringAfter("$key=")
            ?.substringBefore(";")
            ?.trim()
    }

    override fun getCurrentUserId(): String? {
        val currentUser = firebaseAuth.currentUser
        return currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    override fun getCurrentUserPhotoUrl(): String? {
        return firebaseAuth.currentUser?.photoUrl?.toString()
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}