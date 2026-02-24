package com.aos.fanpulse.data.remote

import com.aos.fanpulse.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Provider
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val authRepository: AuthRepository,
    // AuthService는 토큰 갱신 API를 호출하는 Retrofit 인터페이스입니다.
    // Provider를 사용하는 이유는 NetworkModule 내에서 서로 참조하는 순환 참조를 막기 위함입니다.
    private val authServiceProvider: Provider<AuthApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. 이미 재시도를 한 요청인지 확인 (무한 루프 방지)
        if (response.count() >= 2) return null

        return runBlocking {
            // 2. 저장소에서 Refresh Token 꺼내기
            val refreshToken = authRepository.refreshToken.first()

            if (!refreshToken.isNullOrEmpty()) {
                // 3. 서버에 새 토큰 요청 (동기적 실행)
                // 서버가 쿠키 기반이므로 요청 시 Cookie 헤더에 리프레시 토큰을 실어 보냅니다.
                val refreshResponse = authServiceProvider.get()
                    .refreshTokens("fanpulse_refresh_token=$refreshToken")
                    .execute()

                if (refreshResponse.isSuccessful) {
                    // 4. 응답 헤더의 Set-Cookie에서 새 토큰들 추출
                    val cookies = refreshResponse.headers().values("Set-Cookie")
                    val newAccess = extractToken(cookies, "fanpulse_access_token")
                    val newRefresh = extractToken(cookies, "fanpulse_refresh_token")

                    if (newAccess != null && newRefresh != null) {
                        // 5. DataStore 업데이트
                        authRepository.updateTokens(newAccess, newRefresh)

                        // 6. 실패했던 원래 요청에 새 액세스 토큰 쿠키를 입혀서 다시 보냄
                        return@runBlocking response.request.newBuilder()
                            .header("Cookie", "fanpulse_access_token=$newAccess")
                            .build()
                    }
                }
            }

            // 갱신 실패 시 (Refresh Token 만료 등) 모든 정보 삭제 및 null 반환 (재시도 중단)
            authRepository.clearAll()
            null
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
}