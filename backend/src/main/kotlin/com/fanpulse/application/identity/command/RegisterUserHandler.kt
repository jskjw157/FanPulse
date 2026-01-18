package com.fanpulse.application.identity.command

import com.fanpulse.application.identity.EmailAlreadyExistsException
import com.fanpulse.application.identity.UsernameAlreadyExistsException
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * Handler for RegisterUserCommand.
 *
 * Responsibilities:
 * - Validate email and username uniqueness
 * - Create User aggregate
 * - Create default UserSettings
 * - Publish domain events
 * - Return created User
 */
@Component
class RegisterUserHandler(
    private val userPort: UserPort,
    private val userSettingsPort: UserSettingsPort,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: DomainEventPublisher
) {

    @Transactional
    fun handle(command: RegisterUserCommand): User {
        logger.debug { "Handling RegisterUserCommand: ${command.email}" }

        // Validate uniqueness
        if (userPort.existsByEmail(command.email)) {
            throw EmailAlreadyExistsException(command.email)
        }
        if (userPort.existsByUsername(command.username)) {
            throw UsernameAlreadyExistsException(command.username)
        }

        // Create user
        val encodedPassword = passwordEncoder.encode(command.password)
        val user = User.register(
            email = Email.of(command.email),
            username = Username.of(command.username),
            encodedPassword = encodedPassword
        )

        // Save user
        val savedUser = userPort.save(user)

        // Publish domain events
        eventPublisher.publishAll(savedUser.pullDomainEvents())

        // Create default settings
        val settings = UserSettings.createDefault(savedUser.id)
        userSettingsPort.save(settings)

        logger.info { "User registered successfully: ${savedUser.id}" }

        return savedUser
    }
}
