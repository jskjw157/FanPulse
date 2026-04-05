package com.fanpulse.application.service.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.application.identity.InvalidTokenException
import com.fanpulse.application.identity.RefreshTokenReusedException
import com.fanpulse.application.identity.command.GoogleLoginCommand
import com.fanpulse.application.identity.command.GoogleLoginHandler
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * 인증 서비스 구현체.
 * Google OAuth 로그인 및 토큰 관리를 지원한다.
 */
@Service
class AuthServiceImpl(
    private val userPort: UserPort,
    private val tokenPort: TokenPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val googleLoginHandler: GoogleLoginHandler
) : AuthService {

    companion object {
        private const val REFRESH_TOKEN_EXPIRATION_DAYS = 7L
        // TokenPort의 액세스 토큰 만료 설정(jwt.access-token-expiration)과 반드시 동기화해야 한다
        private const val ACCESS_TOKEN_EXPIRATION_SECONDS = 3600L
    }

    /**
     * Google OAuth로 사용자를 인증한다.
     *
     * @param request Google 로그인 요청 (ID 토큰 포함)
     * @return 사용자 정보와 JWT 토큰이 담긴 응답
     * @throws com.fanpulse.application.identity.InvalidGoogleTokenException Google 토큰 검증 실패 시
     * @throws com.fanpulse.application.identity.OAuthEmailNotVerifiedException Google에서 이메일 미인증 시
     */
    @Transactional
    override fun googleLogin(request: GoogleLoginRequest): AuthResponse {
        logger.debug { "Google 로그인 시도" }

        val command = GoogleLoginCommand(idToken = request.idToken)
        val user = googleLoginHandler.handle(command)

        val accessToken = tokenPort.generateAccessToken(user.id)
        val refreshTokenStr = tokenPort.generateRefreshToken(user.id)

        val expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60)
        refreshTokenPort.save(user.id, refreshTokenStr, expiresAt)

        logger.info { "Google 로그인 성공: ${user.id}" }

        return AuthResponse(
            userId = user.id,
            email = user.email,
            username = user.username,
            accessToken = accessToken,
            refreshToken = refreshTokenStr
        )
    }

    /**
     * RefreshTokenRequest를 이용해 액세스 토큰을 갱신한다.
     */
    @Transactional
    override fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        return refreshToken(request.refreshToken)
    }

    /**
     * 리프레시 토큰 문자열을 직접 사용해 액세스 토큰을 갱신한다.
     * 리프레시 토큰 로테이션을 구현한다.
     *
     * @param refreshToken 유효한 리프레시 토큰
     * @return 새 액세스 토큰과 리프레시 토큰이 담긴 응답
     * @throws InvalidTokenException 토큰이 유효하지 않거나 리프레시 토큰이 아닌 경우
     * @throws RefreshTokenReusedException 이미 사용된 토큰이 재사용된 경우 (보안 침해 감지)
     */
    @Transactional
    override fun refreshToken(refreshToken: String): TokenResponse {
        logger.debug { "토큰 갱신 요청" }

        if (!tokenPort.validateToken(refreshToken)) {
            throw InvalidTokenException("토큰이 유효하지 않거나 만료되었습니다.")
        }

        val tokenType = tokenPort.getTokenType(refreshToken)
        if (tokenType != "refresh") {
            throw InvalidTokenException("리프레시 토큰이 아닙니다.")
        }

        val userId = tokenPort.getUserIdFromToken(refreshToken)
        val tokenRecord = refreshTokenPort.findByToken(refreshToken)

        if (tokenRecord == null) {
            logger.warn { "리프레시 토큰이 로테이션 저장소에 없음. 사용자: $userId" }
        } else if (tokenRecord.invalidated) {
            logger.warn { "리프레시 토큰 재사용 감지. 사용자: $userId - 모든 토큰 무효화" }
            refreshTokenPort.invalidateAllByUserId(userId)
            throw RefreshTokenReusedException()
        } else {
            refreshTokenPort.invalidate(refreshToken)
        }

        val user = userPort.findById(userId)
            ?: throw InvalidTokenException("사용자를 찾을 수 없습니다.")

        val newAccessToken = tokenPort.generateAccessToken(userId)
        val newRefreshToken = tokenPort.generateRefreshToken(userId)

        val expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60)
        refreshTokenPort.save(userId, newRefreshToken, expiresAt)

        logger.debug { "토큰 갱신 완료. 사용자: $userId" }

        return TokenResponse(
            accessToken = newAccessToken,
            expiresIn = ACCESS_TOKEN_EXPIRATION_SECONDS,
            refreshToken = newRefreshToken
        )
    }

    /**
     * 사용자의 모든 리프레시 토큰을 무효화하여 로그아웃 처리한다.
     *
     * @param userId 로그아웃할 사용자 ID
     */
    @Transactional
    override fun logout(userId: UUID) {
        logger.debug { "로그아웃 요청. 사용자: $userId" }
        refreshTokenPort.invalidateAllByUserId(userId)
        logger.info { "로그아웃 완료. 사용자: $userId" }
    }

    /**
     * 액세스 토큰의 유효성을 검증하고 사용자 ID를 반환한다.
     *
     * @param token 검증할 액세스 토큰
     * @return 토큰이 유효하면 사용자 ID
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    override fun validateAccessToken(token: String): UUID {
        if (!tokenPort.validateToken(token)) {
            throw InvalidTokenException()
        }

        val tokenType = tokenPort.getTokenType(token)
        if (tokenType != "access") {
            throw InvalidTokenException("액세스 토큰이 아닙니다.")
        }

        return tokenPort.getUserIdFromToken(token)
    }

    /**
     * 액세스 토큰의 유효성을 검증하고 사용자 정보를 반환한다.
     *
     * @param token 검증할 액세스 토큰
     * @return 인증된 사용자 정보
     * @throws InvalidTokenException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    override fun validateTokenAndGetUser(token: String): UserInfo {
        val userId = validateAccessToken(token)
        val user = userPort.findById(userId)
            ?: throw InvalidTokenException("사용자를 찾을 수 없습니다.")

        return UserInfo(
            id = user.id,
            email = user.email,
            username = user.username
        )
    }
}
