package com.fanpulse.application.identity

import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.event.LoginType
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.port.OAuthAccountPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * Authentication and Authorization Service.
 *
 * Handles user registration, login, and token management.
 */
@Service
class AuthService(
    private val userPort: UserPort,
    private val userSettingsPort: UserSettingsPort,
    private val oAuthAccountPort: OAuthAccountPort,
    private val tokenPort: TokenPort,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: DomainEventPublisher
) {

    /**
     * Registers a new user with email/password.
     *
     * @param request Registration request containing email, username, and password
     * @return AuthResponse with tokens and user info
     * @throws EmailAlreadyExistsException if email is already registered
     * @throws UsernameAlreadyExistsException if username is already taken
     */
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        logger.debug { "Registering new user: ${request.email}" }

        // Validate uniqueness
        if (userPort.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException(request.email)
        }
        if (userPort.existsByUsername(request.username)) {
            throw UsernameAlreadyExistsException(request.username)
        }

        // Create user
        val encodedPassword = passwordEncoder.encode(request.password)
        val user = User.register(
            email = Email.of(request.email),
            username = Username.of(request.username),
            encodedPassword = encodedPassword
        )

        // Save user
        val savedUser = userPort.save(user)

        // Publish domain events
        eventPublisher.publishAll(savedUser.pullDomainEvents())

        // Create default settings
        val settings = UserSettings.createDefault(savedUser.id)
        userSettingsPort.save(settings)

        // Generate tokens
        val accessToken = tokenPort.generateAccessToken(savedUser.id)
        val refreshToken = tokenPort.generateRefreshToken(savedUser.id)

        logger.info { "User registered successfully: ${savedUser.id}" }

        return AuthResponse(
            userId = savedUser.id,
            email = savedUser.email,
            username = savedUser.username,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    /**
     * Authenticates a user with email/password.
     *
     * @param request Login request containing email and password
     * @return AuthResponse with tokens and user info
     * @throws InvalidCredentialsException if credentials are invalid
     */
    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        logger.debug { "Login attempt for: ${request.email}" }

        // Find user
        val user = userPort.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        // Verify password
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            logger.debug { "Invalid password for: ${request.email}" }
            throw InvalidCredentialsException()
        }

        // Publish login event
        eventPublisher.publish(
            UserLoggedIn(
                userId = user.id,
                loginType = LoginType.EMAIL,
                ipAddress = null,  // TODO: Extract from HttpServletRequest
                userAgent = null
            )
        )

        // Generate tokens
        val accessToken = tokenPort.generateAccessToken(user.id)
        val refreshToken = tokenPort.generateRefreshToken(user.id)

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
     *
     * @param refreshToken Valid refresh token
     * @return TokenResponse with new access and refresh tokens
     * @throws InvalidTokenException if token is invalid or not a refresh token
     */
    @Transactional(readOnly = true)
    fun refreshToken(refreshToken: String): TokenResponse {
        logger.debug { "Token refresh request" }

        // Validate token
        if (!tokenPort.validateToken(refreshToken)) {
            throw InvalidTokenException("Token is invalid or expired")
        }

        // Verify it's a refresh token
        val tokenType = tokenPort.getTokenType(refreshToken)
        if (tokenType != "refresh") {
            throw InvalidTokenException("Not a refresh token")
        }

        // Get user ID and verify user exists
        val userId = tokenPort.getUserIdFromToken(refreshToken)
        val user = userPort.findById(userId)
            ?: throw InvalidTokenException("User not found")

        // Generate new tokens
        val newAccessToken = tokenPort.generateAccessToken(userId)
        val newRefreshToken = tokenPort.generateRefreshToken(userId)

        logger.debug { "Tokens refreshed for user: $userId" }

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
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
