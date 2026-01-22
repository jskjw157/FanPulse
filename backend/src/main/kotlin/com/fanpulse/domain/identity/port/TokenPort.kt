package com.fanpulse.domain.identity.port

import java.util.*

/**
 * Domain Port for Token operations.
 * 프레임워크 독립적인 토큰 생성 및 검증 인터페이스
 *
 * 이 인터페이스를 통해 AuthService는 Infrastructure의 JWT 구현체에 의존하지 않습니다.
 * (Dependency Inversion Principle)
 */
interface TokenPort {

    /**
     * Access Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT Access Token
     */
    fun generateAccessToken(userId: UUID): String

    /**
     * Refresh Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT Refresh Token
     */
    fun generateRefreshToken(userId: UUID): String

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun validateToken(token: String): Boolean

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    fun getUserIdFromToken(token: String): UUID

    /**
     * 토큰의 타입을 반환합니다 (access 또는 refresh).
     *
     * @param token JWT 토큰
     * @return 토큰 타입 ("access" 또는 "refresh")
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    fun getTokenType(token: String): String
}
