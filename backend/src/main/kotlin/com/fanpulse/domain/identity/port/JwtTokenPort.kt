package com.fanpulse.domain.identity.port

import java.util.Date
import java.util.UUID

/**
 * JWT 토큰 관리를 위한 포트 인터페이스
 *
 * 이 인터페이스는 도메인 계층에서 JWT 토큰 생성/검증을 추상화합니다.
 * Application 계층은 이 인터페이스에만 의존하며, Infrastructure 계층에서 구현합니다.
 *
 * Clean Architecture 원칙:
 * - 도메인 계층은 외부 프레임워크(jjwt)에 의존하지 않습니다
 * - 의존성 역전(DIP)을 통해 유연성과 테스트 용이성을 확보합니다
 */
interface JwtTokenPort {

    /**
     * 사용자 ID로 액세스 토큰을 생성합니다.
     *
     * @param userId 토큰에 포함할 사용자 식별자
     * @return 생성된 JWT 액세스 토큰 문자열
     */
    fun generateAccessToken(userId: UUID): String

    /**
     * 사용자 ID로 리프레시 토큰을 생성합니다.
     *
     * @param userId 토큰에 포함할 사용자 식별자
     * @return 생성된 JWT 리프레시 토큰 문자열
     */
    fun generateRefreshToken(userId: UUID): String

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 추출된 사용자 ID, 파싱 실패 시 null
     */
    fun getUserIdFromToken(token: String): UUID?

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * 검증 항목:
     * - 서명 유효성
     * - 만료 시간
     * - 토큰 형식
     *
     * @param token JWT 토큰 문자열
     * @return 유효한 토큰이면 true, 그렇지 않으면 false
     */
    fun validateToken(token: String): Boolean

    /**
     * 토큰이 액세스 토큰인지 확인합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 액세스 토큰이면 true
     */
    fun isAccessToken(token: String): Boolean

    /**
     * 토큰이 리프레시 토큰인지 확인합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 리프레시 토큰이면 true
     */
    fun isRefreshToken(token: String): Boolean

    /**
     * 토큰의 만료 시간을 가져옵니다.
     *
     * @param token JWT 토큰 문자열
     * @return 만료 시간(Date), 파싱 실패 시 null
     */
    fun getExpirationFromToken(token: String): Date?
}
