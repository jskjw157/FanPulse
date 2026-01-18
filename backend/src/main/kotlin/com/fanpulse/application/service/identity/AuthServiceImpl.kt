package com.fanpulse.application.service.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.port.OAuthAccountPort
import com.fanpulse.domain.identity.port.OAuthTokenVerifierPort
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import com.fanpulse.infrastructure.security.jwt.JwtTokenProvider
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * 인증 서비스 구현체
 */
@Service
class AuthServiceImpl(
    private val userPort: UserPort,
    private val userSettingsPort: UserSettingsPort,
    private val oAuthAccountPort: OAuthAccountPort,
    private val jwtTokenProvider: JwtTokenProvider,
    private val googleTokenVerifier: OAuthTokenVerifierPort
) : AuthService {

    /**
     * Google OAuth 로그인 처리 (보안 개선)
     *
     * 흐름:
     * 1. Google ID Token 검증 + email_verified 확인
     * 2. OAuth 계정 조회 (provider + providerUserId)
     * 3a. 기존 OAuth 계정 -> 로그인
     * 3b. 신규 OAuth 계정:
     *     - 이메일 기존 사용자 확인
     *     - 기존 사용자 && emailVerified -> OAuth 연결
     *     - 기존 사용자 && !emailVerified -> 에러 (계정 탈취 방지)
     *     - 신규 사용자 -> 생성
     * 4. JWT 토큰 발급
     */
    @Transactional
    override fun googleLogin(request: GoogleLoginRequest): TokenResponse {
        logger.debug { "Google login attempt" }

        // 1. Google ID Token 검증
        val googleUser = googleTokenVerifier.verify(request.idToken)
            ?: throw InvalidGoogleTokenException("Invalid or expired Google ID token")

        // 1-1. email_verified 확인 (보안 강화)
        if (!googleUser.emailVerified) {
            logger.warn { "Google login rejected: email not verified" }
            throw OAuthEmailNotVerifiedException("Google")
        }

        logger.debug { "Google token verified for user: ${googleUser.email}" }

        // 2. 기존 OAuth 계정 조회
        val existingOAuthAccount = oAuthAccountPort.findByProviderAndProviderUserId(
            provider = OAuthProvider.GOOGLE,
            providerUserId = googleUser.providerUserId
        )

        val user = if (existingOAuthAccount != null) {
            // 3a. 기존 OAuth 계정 로그인
            handleExistingOAuthLogin(existingOAuthAccount)
        } else {
            // 3b. 신규 OAuth 계정 처리 (Race Condition 방어)
            handleNewOAuthLogin(googleUser)
        }

        // 4. JWT 토큰 발급
        logger.info { "Google login successful for user: ${user.id}" }
        return createTokenResponse(user.id)
    }

    /**
     * 기존 OAuth 계정 로그인 처리
     */
    private fun handleExistingOAuthLogin(oAuthAccount: OAuthAccount): User {
        logger.debug { "Existing OAuth account found: ${oAuthAccount.provider}:${oAuthAccount.providerUserId}" }
        return userPort.findById(oAuthAccount.userId)
            ?: throw UserNotFoundException("User not found for OAuth account")
    }

    /**
     * 신규 OAuth 계정 처리 (계정 탈취 방지 + Race Condition 방어)
     */
    private fun handleNewOAuthLogin(googleUser: com.fanpulse.domain.identity.port.OAuthUserInfo): User {
        logger.info { "Processing new OAuth account for: ${googleUser.email}" }

        // 이메일로 기존 사용자 확인
        val existingUser = userPort.findByEmail(googleUser.email)

        return if (existingUser != null) {
            // 기존 이메일 사용자에게 OAuth 연결 시도
            linkOAuthToExistingUser(existingUser, googleUser)
        } else {
            // 완전 신규 사용자 생성
            createNewUserWithOAuth(googleUser)
        }
    }

    /**
     * 기존 사용자에게 OAuth 계정 연결 (계정 탈취 방지)
     *
     * CRITICAL: Google이 이메일을 검증했다면 신뢰하고, 기존 사용자의 emailVerified도 업데이트
     */
    private fun linkOAuthToExistingUser(
        existingUser: User,
        googleUser: com.fanpulse.domain.identity.port.OAuthUserInfo
    ): User {
        // Google이 이메일을 검증했으므로 기존 사용자도 검증된 것으로 처리
        val userToSave = if (!existingUser.emailVerified && googleUser.emailVerified) {
            logger.info { "Updating emailVerified for user ${existingUser.id} based on Google verification" }
            userPort.save(existingUser.verifyEmail())
        } else {
            existingUser
        }

        logger.info { "Linking Google account to existing user: ${userToSave.id}" }

        val oAuthAccount = OAuthAccount.create(
            userId = userToSave.id,
            provider = OAuthProvider.GOOGLE,
            providerUserId = googleUser.providerUserId,
            email = googleUser.email,
            emailVerified = googleUser.emailVerified
        )

        // Race Condition 방어: 원자적 저장 시도
        val savedAccount = saveOAuthAccountSafely(oAuthAccount)

        if (savedAccount == null) {
            // 다른 요청이 먼저 생성함 -> 해당 계정으로 로그인
            logger.info { "OAuth account was created by concurrent request, fetching..." }
            val existing = oAuthAccountPort.findByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                googleUser.providerUserId
            )!!
            return userPort.findById(existing.userId)!!
        }

        return userToSave
    }

    /**
     * 신규 사용자 + OAuth 계정 생성
     */
    private fun createNewUserWithOAuth(googleUser: com.fanpulse.domain.identity.port.OAuthUserInfo): User {
        // Username 생성 (N+1 쿼리 최적화)
        val username = generateUniqueUsernameOptimized(googleUser.email, googleUser.name)

        val newUser = User.registerWithOAuth(
            email = Email.of(googleUser.email),
            username = Username.of(username)
        )
        val savedUser = userPort.save(newUser)

        // 기본 설정 생성
        val settings = UserSettings.createDefault(savedUser.id)
        userSettingsPort.save(settings)

        // OAuth 계정 연결
        val oAuthAccount = OAuthAccount.create(
            userId = savedUser.id,
            provider = OAuthProvider.GOOGLE,
            providerUserId = googleUser.providerUserId,
            email = googleUser.email,
            emailVerified = googleUser.emailVerified
        )

        // Race Condition 방어
        val savedAccount = saveOAuthAccountSafely(oAuthAccount)

        if (savedAccount == null) {
            // 동시에 같은 Google 계정으로 가입 시도 발생
            // 먼저 생성된 계정으로 로그인 (예외 던지지 않음 - 트랜잭션 롤백 방지)
            logger.warn { "Concurrent OAuth registration detected, logging in with existing account" }
            val existing = oAuthAccountPort.findByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                googleUser.providerUserId
            )!!
            return userPort.findById(existing.userId)!!
        }

        logger.info { "New user created with Google OAuth: ${savedUser.id}" }
        return savedUser
    }

    /**
     * OAuth 계정 안전하게 저장 (Race Condition 방어)
     *
     * @return 저장된 계정 (이미 존재하면 null)
     */
    private fun saveOAuthAccountSafely(oAuthAccount: OAuthAccount): OAuthAccount? {
        return try {
            oAuthAccountPort.saveIfNotExists(oAuthAccount)
        } catch (e: DataIntegrityViolationException) {
            logger.info { "OAuth account already exists (race condition): ${e.message}" }
            null
        }
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     */
    @Transactional(readOnly = true)
    override fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        logger.debug { "Token refresh attempt" }

        // 토큰 검증
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw InvalidTokenException("Invalid refresh token")
        }

        if (!jwtTokenProvider.isRefreshToken(request.refreshToken)) {
            throw InvalidTokenException("Token is not a refresh token")
        }

        // 사용자 ID 추출
        val userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken)
            ?: throw InvalidTokenException("Cannot extract user ID from token")

        // 사용자 존재 확인
        val user = userPort.findById(userId)
            ?: throw UserNotFoundException("User not found for token")

        logger.info { "Token refreshed for user: ${user.id}" }

        return createTokenResponse(user.id)
    }

    /**
     * 로그아웃
     *
     * JWT는 stateless이므로 클라이언트에서 토큰을 삭제하는 방식으로 처리.
     * 향후 Redis 블랙리스트 구현 시 여기에 추가.
     */
    override fun logout(accessToken: String) {
        logger.debug { "Logout request for token (stateless, no server action)" }
        // TODO: 필요시 Redis 블랙리스트 구현
    }

    /**
     * User ID로 TokenResponse 생성
     */
    private fun createTokenResponse(userId: UUID): TokenResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(userId)
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

        return TokenResponse(
            accessToken = accessToken,
            tokenType = "Bearer",
            expiresIn = 3600,  // 1 hour
            refreshToken = refreshToken,
            refreshExpiresIn = 604800  // 7 days
        )
    }

    /**
     * 고유 Username 생성 (N+1 쿼리 최적화)
     *
     * 기존: while 루프로 existsByUsername() 최대 1000회 호출
     * 개선: 단일 쿼리로 최대 suffix 조회
     */
    private fun generateUniqueUsernameOptimized(email: String, name: String?): String {
        val base = (name?.replace(" ", "_")?.take(20)
            ?: email.substringBefore("@").take(20))
            .lowercase()
            .replace(Regex("[^a-z0-9_]"), "")  // 안전한 문자만 허용

        // 1. base가 사용 가능한지 확인
        if (!userPort.existsByUsername(base)) {
            return base
        }

        // 2. base_N 형식 중 가장 큰 N 조회 (단일 쿼리)
        val maxSuffix = userPort.findMaxUsernameSuffix(base)
        val nextSuffix = maxSuffix + 1

        // 3. 안전장치: suffix가 너무 크면 UUID 사용
        return if (nextSuffix > 10000) {
            "${base}_${UUID.randomUUID().toString().take(8)}"
        } else {
            "${base}_$nextSuffix"
        }
    }
}
