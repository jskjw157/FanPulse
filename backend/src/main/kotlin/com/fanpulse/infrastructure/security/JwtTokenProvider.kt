package com.fanpulse.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

private val logger = KotlinLogging.logger {}

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider.
 *
 * Access Token: 짧은 만료 시간 (기본 1시간)
 * Refresh Token: 긴 만료 시간 (기본 7일)
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret:default-secret-key-that-is-at-least-256-bits-long-for-hs256}")
    private val secret: String,

    @Value("\${jwt.access-expiration:3600000}")
    private val accessTokenExpiration: Long,

    @Value("\${jwt.refresh-expiration:604800000}")
    private val refreshTokenExpiration: Long
) {
    init {
        validateSecretKey(secret)
    }

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private fun validateSecretKey(secret: String) {
        require(secret.isNotBlank()) {
            "JWT secret key cannot be blank"
        }

        val keyLengthInBits = secret.toByteArray().size * 8
        require(keyLengthInBits >= 256) {
            "JWT secret key must be at least 256 bits (32 bytes). Current: $keyLengthInBits bits"
        }

        logger.info { "JWT secret key validated: ${keyLengthInBits} bits" }
    }

    companion object {
        private const val TOKEN_TYPE_CLAIM = "type"
        private const val TOKEN_TYPE_ACCESS = "access"
        private const val TOKEN_TYPE_REFRESH = "refresh"
    }

    /**
     * Access Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT Access Token
     */
    fun generateAccessToken(userId: UUID): String {
        val now = Date()
        val expiration = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    /**
     * Refresh Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return JWT Refresh Token
     */
    fun generateRefreshToken(userId: UUID): String {
        val now = Date()
        val expiration = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun validateToken(token: String): Boolean {
        if (token.isBlank()) {
            return false
        }

        return try {
            val claims = parseClaimsJws(token)
            claims != null
        } catch (e: ExpiredJwtException) {
            logger.debug { "Token expired: ${e.message}" }
            false
        } catch (e: JwtException) {
            logger.debug { "Invalid token: ${e.message}" }
            false
        } catch (e: Exception) {
            logger.warn { "Token validation failed: ${e.message}" }
            false
        }
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    fun getUserIdFromToken(token: String): UUID {
        val claims = parseClaimsJws(token)
            ?: throw JwtException("Invalid token")
        return UUID.fromString(claims.subject)
    }

    /**
     * 토큰에서 만료 시간을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 만료 시간
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    fun getExpirationFromToken(token: String): Date {
        val claims = parseClaimsJws(token)
            ?: throw JwtException("Invalid token")
        return claims.expiration
    }

    /**
     * 토큰 타입을 반환합니다 (access 또는 refresh).
     *
     * @param token JWT 토큰
     * @return 토큰 타입 ("access" 또는 "refresh")
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    fun getTokenType(token: String): String {
        val claims = parseClaimsJws(token)
            ?: throw JwtException("Invalid token")
        return claims[TOKEN_TYPE_CLAIM] as? String
            ?: throw JwtException("Token type not found")
    }

    /**
     * 토큰이 Access Token인지 확인합니다.
     * 예외를 던지지 않고 boolean을 반환합니다.
     *
     * @param token JWT 토큰
     * @return Access Token이면 true, 그렇지 않으면 false
     */
    fun isAccessToken(token: String): Boolean {
        return try {
            getTokenType(token) == TOKEN_TYPE_ACCESS
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 토큰이 Refresh Token인지 확인합니다.
     * 예외를 던지지 않고 boolean을 반환합니다.
     *
     * @param token JWT 토큰
     * @return Refresh Token이면 true, 그렇지 않으면 false
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            getTokenType(token) == TOKEN_TYPE_REFRESH
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 토큰을 파싱하여 Claims를 반환합니다.
     *
     * @param token JWT 토큰
     * @return Claims 또는 null (파싱 실패 시)
     */
    private fun parseClaimsJws(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}
