package com.fanpulse.application.identity.command

import com.fanpulse.application.identity.InvalidGoogleTokenException
import com.fanpulse.application.identity.OAuthEmailNotVerifiedException
import com.fanpulse.application.identity.UsernameConflictException
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.event.LoginType
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.port.OAuthAccountPort
import com.fanpulse.domain.identity.port.OAuthTokenVerifierPort
import com.fanpulse.domain.identity.port.OAuthUserInfo
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import mu.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * Handler for GoogleLoginCommand.
 *
 * Responsibilities:
 * - Verify Google ID token
 * - Find or create user based on OAuth account
 * - Handle race conditions for username conflicts
 * - Link OAuth account to existing user if email matches
 * - Publish domain events
 *
 * @throws InvalidGoogleTokenException if token verification fails
 * @throws OAuthEmailNotVerifiedException if Google didn't verify the email
 * @throws UsernameConflictException if username generation fails after retries
 */
@Component
class GoogleLoginHandler(
    private val oAuthTokenVerifier: OAuthTokenVerifierPort,
    private val oAuthAccountPort: OAuthAccountPort,
    private val userPort: UserPort,
    private val userSettingsPort: UserSettingsPort,
    private val eventPublisher: DomainEventPublisher
) {
    companion object {
        private const val MAX_USERNAME_RETRIES = 3
    }

    @Transactional
    fun handle(command: GoogleLoginCommand): User {
        logger.debug { "Handling GoogleLoginCommand" }

        // 1. Verify Google ID token
        val googleUser = oAuthTokenVerifier.verify(command.idToken)
            ?: throw InvalidGoogleTokenException()

        // 2. Check email verification
        if (!googleUser.emailVerified) {
            logger.warn { "Google account email not verified: ${googleUser.email}" }
            throw OAuthEmailNotVerifiedException()
        }

        // 3. Find existing OAuth account
        val existingOAuthAccount = oAuthAccountPort.findByProviderAndProviderUserId(
            provider = OAuthProvider.GOOGLE,
            providerUserId = googleUser.providerUserId
        )

        val user = if (existingOAuthAccount != null) {
            // Existing OAuth account - return the linked user
            handleExistingOAuthAccount(existingOAuthAccount, googleUser)
        } else {
            // New OAuth account
            handleNewOAuthAccount(googleUser)
        }

        // Publish login event
        eventPublisher.publish(
            UserLoggedIn(
                userId = user.id,
                loginType = LoginType.GOOGLE,
                ipAddress = null,  // Can be passed from controller if needed
                userAgent = null
            )
        )

        logger.info { "Google login successful: userId=${user.id}" }
        return user
    }

    private fun handleExistingOAuthAccount(
        oAuthAccount: OAuthAccount,
        googleUser: OAuthUserInfo
    ): User {
        logger.debug { "Existing OAuth account found: ${oAuthAccount.id}" }
        return userPort.findById(oAuthAccount.userId)
            ?: throw IllegalStateException("User not found for OAuth account: ${oAuthAccount.id}")
    }

    private fun handleNewOAuthAccount(googleUser: OAuthUserInfo): User {
        // Check if user with same email already exists
        val existingUser = userPort.findByEmail(googleUser.email)

        return if (existingUser != null) {
            // Link OAuth to existing user
            linkOAuthToExistingUser(existingUser, googleUser)
        } else {
            // Create new user
            createNewUser(googleUser)
        }
    }

    private fun linkOAuthToExistingUser(
        existingUser: User,
        googleUser: OAuthUserInfo
    ): User {
        logger.debug { "Linking OAuth account to existing user: ${existingUser.id}" }

        val oAuthAccount = OAuthAccount.create(
            userId = existingUser.id,
            provider = OAuthProvider.GOOGLE,
            providerUserId = googleUser.providerUserId,
            email = googleUser.email
        )
        oAuthAccountPort.save(oAuthAccount)

        return existingUser
    }

    private fun createNewUser(googleUser: OAuthUserInfo): User {
        logger.debug { "Creating new user for Google account: ${googleUser.email}" }

        return saveNewUserWithRetry(googleUser)
    }

    /**
     * Creates a new user with retry mechanism for username conflicts.
     * Race conditions can occur when multiple requests try to register with same username.
     */
    private fun saveNewUserWithRetry(googleUser: OAuthUserInfo): User {
        var lastException: DataIntegrityViolationException? = null

        repeat(MAX_USERNAME_RETRIES) { attempt ->
            try {
                val username = generateUniqueUsername(googleUser.email, googleUser.name, attempt)
                val user = User.registerWithOAuth(
                    email = Email.of(googleUser.email),
                    username = Username.of(username)
                )

                val savedUser = userPort.save(user)

                // Publish domain events
                eventPublisher.publishAll(savedUser.pullDomainEvents())

                // Create default settings
                val settings = UserSettings.createDefault(savedUser.id)
                userSettingsPort.save(settings)

                // Create OAuth account
                val oAuthAccount = OAuthAccount.create(
                    userId = savedUser.id,
                    provider = OAuthProvider.GOOGLE,
                    providerUserId = googleUser.providerUserId,
                    email = googleUser.email
                )
                oAuthAccountPort.save(oAuthAccount)

                logger.info { "New user created via Google OAuth: ${savedUser.id}" }
                return savedUser

            } catch (e: DataIntegrityViolationException) {
                logger.warn { "Username collision on attempt ${attempt + 1}/$MAX_USERNAME_RETRIES: ${e.message}" }
                lastException = e
            }
        }

        throw UsernameConflictException("Unable to generate unique username after $MAX_USERNAME_RETRIES attempts")
    }

    /**
     * Generates a unique username based on email or name.
     * Adds random suffix on retry attempts.
     */
    private fun generateUniqueUsername(email: String, name: String?, attempt: Int): String {
        val baseUsername = when {
            !name.isNullOrBlank() -> name.replace(Regex("[^a-zA-Z0-9_-]"), "").take(20)
            else -> email.substringBefore("@").replace(Regex("[^a-zA-Z0-9_-]"), "").take(20)
        }.lowercase().ifEmpty { "user" }

        return if (attempt == 0) {
            // First attempt: try base username
            if (userPort.existsByUsername(baseUsername)) {
                "${baseUsername}_${System.currentTimeMillis() % 10000}"
            } else {
                baseUsername
            }
        } else {
            // Retry attempts: add random suffix
            "${baseUsername}_${System.currentTimeMillis() % 100000}"
        }
    }
}
