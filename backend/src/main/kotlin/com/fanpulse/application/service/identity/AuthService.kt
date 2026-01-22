package com.fanpulse.application.service.identity

import com.fanpulse.application.dto.identity.*

/**
 * 인증 서비스 인터페이스
 *
 * Google OAuth 기반 사용자 인증 관련 비즈니스 로직을 정의합니다.
 */
interface AuthService {
    /**
     * Google OAuth 로그인
     */
    fun googleLogin(request: GoogleLoginRequest): TokenResponse

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     */
    fun refreshToken(request: RefreshTokenRequest): TokenResponse

    /**
     * 로그아웃
     */
    fun logout(accessToken: String)
}
