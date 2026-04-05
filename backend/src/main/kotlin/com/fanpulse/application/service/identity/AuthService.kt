package com.fanpulse.application.service.identity

import com.fanpulse.application.dto.identity.*
import java.util.UUID

/**
 * 인증 서비스 인터페이스.
 * 회원가입, 로그인, 토큰 관리를 담당한다.
 */
interface AuthService {

    /**
     * 이메일/비밀번호로 신규 사용자를 등록한다.
     * @throws IllegalArgumentException 이미 존재하는 이메일 또는 사용자명인 경우
     */
    fun signup(request: SignupRequest): TokenResponse

    /**
     * 이메일/비밀번호로 사용자를 인증한다.
     * @throws IllegalArgumentException 자격 증명이 유효하지 않은 경우
     */
    fun login(request: LoginRequest): TokenResponse

    /**
     * Google OAuth로 사용자를 인증한다.
     * 신규 사용자인 경우 자동으로 계정을 생성한다.
     */
    fun googleLogin(request: GoogleLoginRequest): AuthResponse

    /**
     * RefreshTokenRequest를 이용해 액세스 토큰을 갱신한다.
     * @throws IllegalArgumentException 리프레시 토큰이 유효하지 않거나 만료된 경우
     */
    fun refreshToken(request: RefreshTokenRequest): TokenResponse

    /**
     * 리프레시 토큰 문자열을 직접 사용해 액세스 토큰을 갱신한다.
     * 리프레시 토큰 로테이션을 구현한다.
     * @throws IllegalArgumentException 리프레시 토큰이 유효하지 않거나 만료된 경우
     */
    fun refreshToken(refreshToken: String): TokenResponse

    /**
     * 사용자의 토큰을 무효화하여 로그아웃 처리한다.
     * @param userId 로그아웃할 사용자의 ID
     */
    fun logout(userId: UUID)

    /**
     * 액세스 토큰의 유효성을 검증하고 사용자 ID를 반환한다.
     * @param token 검증할 액세스 토큰
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    fun validateAccessToken(token: String): UUID

    /**
     * 액세스 토큰의 유효성을 검증하고 사용자 정보를 반환한다.
     * @param token 검증할 액세스 토큰
     * @throws IllegalArgumentException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    fun validateTokenAndGetUser(token: String): UserInfo
}
