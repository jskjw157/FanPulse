package com.fanpulse.application.identity

import com.fanpulse.application.identity.command.RegisterUserCommand
import com.fanpulse.application.identity.command.RegisterUserHandler
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.event.LoginType
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.port.OAuthAccountPort
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import java.time.Instant
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * Authentication and Authorization Service (Facade).
 *
 * Delegates business logic to Command Handlers and manages token generation.
 */
@Service
class AuthService(
    private val userPort: UserPort,
    private val userSettingsPort: UserSettingsPort,
    private val oAuthAccountPort: OAuthAccountPort,
    private val tokenPort: TokenPort,
    private val refreshTokenPort: RefreshTokenPort,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: DomainEventPublisher,
    private val registerUserHandler: RegisterUserHandler
) {
    companion object {
        private const val REFRESH_TOKEN_EXPIRATION_DAYS = 7L
    }

    /**
     * Registers a new user with email/password.
     *
     * Delegates user creation to RegisterUserHandler and manages token generation.
     *
     * @param request Registration request containing email, username, and password
     * @return AuthResponse with tokens and user info
     * @throws EmailAlreadyExistsException if email is already registered
     * @throws UsernameAlreadyExistsException if username is already taken
     */
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.debug { "Registering new user: ${request.email}" }

        // Create command
        val command = RegisterUserCommand(
            email = request.email,
            username = request.username,
            password = request.password
        )

        // Delegate to handler
        val user = registerUserHandler.handle(command)

        // Generate tokens
        val accessToken = tokenPort.generateAccessToken(user.id)
        val refreshToken = tokenPort.generateRefreshToken(user.id)

        // Store refresh token for rotation tracking
        val expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60)
        refreshTokenPort.save(user.id, refreshToken, expiresAt)

        logger.info { "User registered successfully: ${user.id}" }

        return AuthResponse(
            userId = user.id,
            email = user.email,
            username = user.username,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    /**
     * Authenticates a user with email/password.
     *
     * @param request Login request containing email and password
     * @param requestContext Optional context containing IP address and user agent for audit trail
     * @return AuthResponse with tokens and user info
     * @throws InvalidCredentialsException if credentials are invalid
     */
    @Transactional
    fun login(request: LoginRequest, requestContext: RequestContext? = null): AuthResponse {
        logger.debug { "Login attempt for: ${request.email}" }

        // Find user
        val user = userPort.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        // Verify password
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            logger.debug { "Invalid password for: ${request.email}" }
            throw InvalidCredentialsException()
        }

        // Publish login event with client context
        eventPublisher.publish(
            UserLoggedIn(
                userId = user.id,
                loginType = LoginType.EMAIL,
                ipAddress = requestContext?.ipAddress,
                userAgent = requestContext?.userAgent
            )
        )

        // Generate tokens
        val accessToken = tokenPort.generateAccessToken(user.id)
        val refreshToken = tokenPort.generateRefreshToken(user.id)

        // Store refresh token for rotation tracking
        val expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60)
        refreshTokenPort.save(user.id, refreshToken, expiresAt)

        logger.info { "User logged in successfully: ${user.id}" }

        return AuthResponse(
            userId = user.id,
            email = user.email,
            username = user.username,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    /**
     * Refreshes access token using a valid refresh token.
     * Implements Refresh Token Rotation for security.
     *
     * ## Refresh Token Rotation
     * - 토큰 사용 시 이전 토큰은 무효화하고 새 토큰 발급
     * - 이미 무효화된 토큰 재사용 시 모든 토큰 무효화 (보안 침해 감지)
     *
     * @param refreshToken Valid refresh token
     * @return TokenResponse with new access and refresh tokens
     * @throws InvalidTokenException if token is invalid or not a refresh token
     * @throws RefreshTokenReusedException if token was already used (security breach)
     */
    @Transactional
    fun refreshToken(refreshToken: String): TokenResponse {
        logger.debug { "Token refresh request" }

        // Validate JWT signature and expiration
        if (!tokenPort.validateToken(refreshToken)) {
            throw InvalidTokenException("Token is invalid or expired")
        }

        // Verify it's a refresh token
        val tokenType = tokenPort.getTokenType(refreshToken)
        if (tokenType != "refresh") {
            throw InvalidTokenException("Not a refresh token")
        }

        // Get user ID
        val userId = tokenPort.getUserIdFromToken(refreshToken)

        // Check token in rotation store
        val tokenRecord = refreshTokenPort.findByToken(refreshToken)

        if (tokenRecord == null) {
            // Token not in store - could be old token from before rotation was implemented
            // Allow it but log warning
            logger.warn { "Refresh token not found in rotation store for user: $userId" }
        } else if (tokenRecord.invalidated) {
            // SECURITY BREACH: Token was already used!
            // Invalidate ALL tokens for this user
            logger.warn { "Refresh token reuse detected for user: $userId - invalidating all tokens" }
            refreshTokenPort.invalidateAllByUserId(userId)
            throw RefreshTokenReusedException()
        } else {
            // Invalidate the current token (rotation)
            refreshTokenPort.invalidate(refreshToken)
        }

        // Verify user exists
        val user = userPort.findById(userId)
            ?: throw InvalidTokenException("User not found")

        // Generate new tokens
        val newAccessToken = tokenPort.generateAccessToken(userId)
        val newRefreshToken = tokenPort.generateRefreshToken(userId)

        // Store new refresh token
        val expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRATION_DAYS * 24 * 60 * 60)
        refreshTokenPort.save(userId, newRefreshToken, expiresAt)

        logger.debug { "Tokens refreshed for user: $userId" }

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    /**
     * Logs out a user by invalidating all their refresh tokens.
     *
     * @param userId User ID to logout
     */
    @Transactional
    fun logout(userId: java.util.UUID) {
        logger.debug { "Logout request for user: $userId" }
        refreshTokenPort.invalidateAllByUserId(userId)
        logger.info { "User logged out: $userId" }
    }

    /**
     * Validates an access token and returns the user ID.
     *
     * @param token Access token to validate
     * @return User ID if valid
     * @throws InvalidTokenException if token is invalid
     */
    fun validateAccessToken(token: String): java.util.UUID {
        if (!tokenPort.validateToken(token)) {
            throw InvalidTokenException()
        }

        val tokenType = tokenPort.getTokenType(token)
        if (tokenType != "access") {
            throw InvalidTokenException("Not an access token")
        }

        return tokenPort.getUserIdFromToken(token)
    }
}
