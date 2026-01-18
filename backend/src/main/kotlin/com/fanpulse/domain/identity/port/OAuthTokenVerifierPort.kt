package com.fanpulse.domain.identity.port

import com.fanpulse.domain.identity.OAuthProvider

/**
 * OAuth Token 검증 결과
 */
data class OAuthUserInfo(
    val provider: OAuthProvider,
    val providerUserId: String,  // sub (Google), id (Kakao) 등
    val email: String,
    val emailVerified: Boolean,
    val name: String?,
    val pictureUrl: String?
)

/**
 * OAuth Token Verifier Port (입력 포트)
 *
 * Application Layer에서 사용하는 OAuth 토큰 검증 인터페이스.
 * Infrastructure Layer에서 구현하여 아키텍처 의존성 방향 준수.
 */
interface OAuthTokenVerifierPort {
    /**
     * OAuth 토큰 검증 및 사용자 정보 추출
     *
     * @param idToken OAuth Provider에서 발급한 ID Token
     * @return 검증 성공 시 사용자 정보, 실패 시 null
     */
    fun verify(idToken: String): OAuthUserInfo?

    /**
     * 지원하는 OAuth Provider
     */
    fun supportedProvider(): OAuthProvider
}
