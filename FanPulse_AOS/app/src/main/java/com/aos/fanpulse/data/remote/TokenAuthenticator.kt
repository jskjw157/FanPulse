package com.aos.fanpulse.data.remote

import com.aos.fanpulse.data.remote.apiservice.AuthenticationApiService
import com.aos.fanpulse.data.remote.apiservice.RefreshRequest
import com.aos.fanpulse.domain.repository.AuthenticationRepository
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
    private val authRepository: AuthenticationRepository,
    // Provider를 사용하는 이유는 NetworkModule 내에서 서로 참조하는 순환 참조를 막기 위함입니다.
    private val authServiceProvider: Provider<AuthenticationApiService>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.count() >= 2) return null

        return runBlocking {
            mutex.withLock {
                // 1. 내가 자물쇠를 기다리는 동안, 앞선 다른 요청이 이미 토큰을 갱신해 두었는지 확인!
                // 실패했던 원본 요청의 토큰과 현재 DataStore에 있는 토큰을 비교합니다.
                val failedToken = extractTokenFromRequest(response.request, "fanpulse_access_token")
                val currentToken = authRepository.accessToken.first() // accessToken도 저장한다고 가정

                // 만약 두 토큰이 다르다면? 앞선 요청이 이미 새 토큰을 받아와 저장한 것입니다.
                if (currentToken != null && failedToken != currentToken) {
                    // 서버에 갱신 요청을 또 할 필요 없이, 방금 갱신된 새 토큰으로 바로 다시 쏘면 됩니다!
                    return@runBlocking response.request.newBuilder()
                        .header("Cookie", "fanpulse_access_token=$currentToken")
                        .build()
                }

                // 2. 아무도 갱신하지 않았다면, 내가 직접 갱신하러 갑니다.
                val refreshToken = authRepository.refreshToken.first()
                if (!refreshToken.isNullOrEmpty()) {
                    // 2. 서버 명세에 맞춰 요청 객체 생성
                    val refreshRequest = RefreshRequest(refreshToken = refreshToken)

                    // 3. 토큰 갱신 API 호출 (인터페이스가 suspend인 경우)
                    // 주의: .execute()는 Call<T>일 때만 사용하므로,
                    // 인터페이스가 suspend라면 별도의 처리가 필요하거나
                    // 전용 동기용 API를 따로 만드는 것이 좋습니다.
                    val refreshResponse = authServiceProvider.get().refreshTokens(refreshRequest)

                    if (refreshResponse.isSuccessful) {
                        // 4. 헤더에서 쿠키 추출
                        val cookies = refreshResponse.headers().values("Set-Cookie")
                        val newAccess = extractToken(cookies, "fanpulse_access_token")
                        val newRefresh = extractToken(cookies, "fanpulse_refresh_token")

                        if (newAccess != null && newRefresh != null) {
                            // 5. 로컬 저장소 업데이트 (코루틴 스코프 확인 필요)
                            authRepository.updateTokens(newAccess, newRefresh)

                            // 6. 실패했던 원래 요청(originalRequest)을 새 토큰으로 수정하여 재시도
                            return@runBlocking response.request.newBuilder()
                                .header("Cookie", "fanpulse_access_token=$newAccess")
                                .build()
                        }
                    }
                }

                // 갱신 실패 시
                authRepository.clearAll()
                null
            }
        }
    }

    // 재시도 횟수를 체크하기 위한 확장 함수
    private fun Response.count(): Int {
        var result = 1
        var lastResponse = this.priorResponse
        while (lastResponse != null) {
            result++
            lastResponse = lastResponse.priorResponse
        }
        return result
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