package com.fanpulse.domain.identity.port

import java.time.Instant
import java.util.*

/**
 * 토큰 원자적 무효화 결과.
 * CAS(Compare-And-Swap) 패턴으로 동시 요청 시 Race Condition을 방지한다.
 */
sealed class TokenInvalidationResult {
    /** 토큰이 원자적으로 무효화됨 (정상 처리) */
    data object Invalidated : TokenInvalidationResult()
    /** 이미 무효화된 토큰 재사용 감지 (보안 침해 가능성) */
    data object AlreadyInvalidated : TokenInvalidationResult()
    /** 저장소에 토큰이 존재하지 않음 */
    data object NotFound : TokenInvalidationResult()
}

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
     * 토큰을 원자적으로 조회하고 무효화합니다 (CAS 패턴).
     *
     * `UPDATE ... WHERE token = ? AND invalidated = false` 쿼리로
     * DB 레벨에서 동시 요청 간 Race Condition을 방지한다.
     *
     * @param token 무효화할 Refresh Token
     * @return 무효화 결과 (성공 / 이미 무효화 / 미등록)
     */
    fun findAndInvalidateByToken(token: String): TokenInvalidationResult

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
