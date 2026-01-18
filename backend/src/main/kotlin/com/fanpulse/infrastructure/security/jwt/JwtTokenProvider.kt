package com.fanpulse.infrastructure.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * JWT 토큰 제공자
 *
 * JWT 토큰의 생성과 검증을 담당합니다.
 */
@Component
class JwtTokenProvider(
    @Value("\${fanpulse.jwt.secret}")
    private val secret: String,

    @Value("\${fanpulse.jwt.access-expiration}")
    private val accessExpiration: Long,

    @Value("\${fanpulse.jwt.refresh-expiration}")
    private val refreshExpiration: Long
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    /**
     * 액세스 토큰 생성
     */
    fun generateAccessToken(userId: UUID): String {
        val now = Instant.now()
        val expiryDate = Date.from(now.plusMillis(accessExpiration))

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(expiryDate)
            .claim("type", "access")
            .signWith(key)
            .compact()
    }

    /**
     * 리프레시 토큰 생성
     */
    fun generateRefreshToken(userId: UUID): String {
        val now = Instant.now()
        val expiryDate = Date.from(now.plusMillis(refreshExpiration))

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(expiryDate)
            .claim("type", "refresh")
            .signWith(key)
            .compact()
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): UUID? {
        return try {
            val claims = getClaims(token)
            UUID.fromString(claims.subject)
        } catch (e: Exception) {
            logger.warn(e) { "Failed to extract user ID from token" }
            null
        }
    }

    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            logger.debug(e) { "Token validation failed" }
            false
        }
    }

    /**
     * 액세스 토큰인지 확인
     */
    fun isAccessToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims["type"] == "access"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 리프레시 토큰인지 확인
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 토큰 만료 시간 가져오기
     */
    fun getExpirationFromToken(token: String): Date? {
        return try {
            val claims = getClaims(token)
            claims.expiration
        } catch (e: Exception) {
            logger.warn(e) { "Failed to get expiration from token" }
            null
        }
    }

    /**
     * 토큰에서 Claims 추출
     */
    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
