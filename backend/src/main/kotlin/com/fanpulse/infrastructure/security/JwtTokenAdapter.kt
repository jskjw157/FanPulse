package com.fanpulse.infrastructure.security

import com.fanpulse.domain.identity.port.TokenPort
import io.jsonwebtoken.JwtException
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * JWT Token Provider Adapter.
 * TokenPort 인터페이스를 구현하여 JwtTokenProvider를 감쌉니다.
 *
 * 이를 통해 Application Layer (AuthService)는 Infrastructure (JwtTokenProvider)에
 * 직접 의존하지 않고 Domain Port (TokenPort)에만 의존합니다.
 */
@Component
class JwtTokenAdapter(
    private val jwtTokenProvider: JwtTokenProvider
) : TokenPort {

    override fun generateAccessToken(userId: UUID): String {
        logger.debug { "Generating access token for user: $userId" }
        return jwtTokenProvider.generateAccessToken(userId)
    }

    override fun generateRefreshToken(userId: UUID): String {
        logger.debug { "Generating refresh token for user: $userId" }
        return jwtTokenProvider.generateRefreshToken(userId)
    }

    override fun validateToken(token: String): Boolean {
        return jwtTokenProvider.validateToken(token)
    }

    override fun getUserIdFromToken(token: String): UUID {
        return try {
            jwtTokenProvider.getUserIdFromToken(token)
        } catch (e: JwtException) {
            logger.debug { "Failed to extract user ID from token: ${e.message}" }
            throw IllegalArgumentException("Invalid token", e)
        } catch (e: Exception) {
            logger.warn { "Unexpected error extracting user ID from token: ${e.message}" }
            throw IllegalArgumentException("Invalid token", e)
        }
    }

    override fun getTokenType(token: String): String {
        return try {
            jwtTokenProvider.getTokenType(token)
        } catch (e: JwtException) {
            logger.debug { "Failed to extract token type: ${e.message}" }
            throw IllegalArgumentException("Invalid token", e)
        } catch (e: Exception) {
            logger.warn { "Unexpected error extracting token type: ${e.message}" }
            throw IllegalArgumentException("Invalid token", e)
        }
    }
}
