package com.fanpulse.domain.identity.port

import java.time.Instant
import java.util.*

/**
 * Domain Port for Refresh Token operations.
 * Refresh Token Rotation을 위한 토큰 저장/무효화 인터페이스
 *
 * ## Refresh Token Rotation이란?
 * - Refresh Token 사용 시 새 토큰을 발급하고 이전 토큰을 무효화
 * - 토큰 탈취 시 피해 최소화 (탈취된 토큰은 1회 사용 후 무효화)
 * - 재사용 탐지 시 모든 토큰 무효화 (보안 침해 감지)
 */
interface RefreshTokenPort {

    /**
     * 새로운 Refresh Token을 저장합니다.
     *
     * @param userId 사용자 ID
     * @param token Refresh Token 값
     * @param expiresAt 만료 시간
     */
    fun save(userId: UUID, token: String, expiresAt: Instant)

    /**
     * 토큰으로 Refresh Token 레코드를 조회합니다.
     *
     * @param token Refresh Token 값
     * @return RefreshTokenRecord 또는 null
     */
    fun findByToken(token: String): RefreshTokenRecord?

    /**
     * 특정 Refresh Token을 무효화합니다.
     *
     * @param token 무효화할 토큰
     */
    fun invalidate(token: String)

    /**
     * 사용자의 모든 Refresh Token을 무효화합니다.
     * (로그아웃, 보안 침해 감지 시 사용)
     *
     * @param userId 사용자 ID
     */
    fun invalidateAllByUserId(userId: UUID)

    /**
     * 만료된 토큰들을 정리합니다 (배치 작업용).
     *
     * @return 삭제된 토큰 수
     */
    fun deleteExpiredTokens(): Int
}

/**
 * Refresh Token 저장소 레코드
 */
data class RefreshTokenRecord(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: Instant,
    val invalidated: Boolean = false,
    val createdAt: Instant = Instant.now()
)
