package com.aos.fanpulse.data.remote

import dagger.Lazy
import com.aos.fanpulse.data.remote.apiservice.AuthenticationApiService
import com.aos.fanpulse.data.remote.dto.RefreshRequest
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Provider
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    // AuthService는 토큰 갱신 API를 호출하는 Retrofit 인터페이스입니다.
    private val authRepository: Lazy<AuthenticationRepository>,
    // Provider를 사용하는 이유는 NetworkModule 내에서 서로 참조하는 순환 참조를 막기 위함입니다.
    private val authServiceProvider: Provider<AuthenticationApiService>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.count() >= 2) return null

        return runBlocking(Dispatchers.IO) {
            mutex.withLock {
                val failedToken = extractTokenFromRequest(response.request, "fanpulse_access_token")

                val currentToken = authRepository.get().authTokens.first().accessToken

                if (currentToken != null && failedToken != currentToken) {
                    return@runBlocking response.request.newBuilder()
                        .header("Cookie", "fanpulse_access_token=$currentToken")
                        .build()
                }

                val refreshToken = authRepository.get().authTokens.first().refreshToken
                if (!refreshToken.isNullOrEmpty()) {
                    val refreshRequest = RefreshRequest(refreshToken = refreshToken)

                    val refreshResponse = authServiceProvider.get().refreshTokens(refreshRequest)

                    if (refreshResponse.isSuccessful) {
                        val cookies = refreshResponse.headers().values("Set-Cookie")
                        val newAccess = extractToken(cookies, "fanpulse_access_token")
                        val newRefresh = extractToken(cookies, "fanpulse_refresh_token")

                        if (newAccess != null && newRefresh != null) {
                            authRepository.get().updateTokens(newAccess, newRefresh)
                            return@runBlocking response.request.newBuilder()
                                .header("Cookie", "fanpulse_access_token=$newAccess")
                                .build()
                        }
                    }
                }
                authRepository.get().clearAll()
                return@runBlocking null
            }
        }
    }

    // 재시도 횟수를 체크하기 위한 확장 함수
    private fun Response.count(): Int {
        var count = 1
        var res: Response? = this
        while (res?.priorResponse != null) { count++; res = res.priorResponse }
        return count
    }

    private fun extractToken(cookies: List<String>, key: String): String? {
        return cookies.find { it.contains(key) }
            ?.substringAfter("$key=")
            ?.substringBefore(";")
            ?.trim()
    }

    // 원본 요청(Request)의 Header에서 특정 토큰을 뽑아오는 헬퍼 함수
    private fun extractTokenFromRequest(request: Request, key: String): String? {
        val cookieHeader = request.header("Cookie") ?: return null
        return cookieHeader.split(";")
            .find { it.trim().startsWith("$key=") }
            ?.substringAfter("$key=")
            ?.trim()
    }
}