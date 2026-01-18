package com.fanpulse.application.identity.command

import java.util.UUID

/**
 * CQRS Command objects for Identity context.
 *
 * Commands represent user intentions to change the system state.
 */

/**
 * Register a new user with email/password.
 */
data class RegisterUserCommand(
    val email: String,
    val username: String,
    val password: String
)

/**
 * Change user's password.
 */
data class ChangePasswordCommand(
    val userId: UUID,
    val currentPassword: String,
    val newPassword: String
)

/**
 * Update user's profile information.
 */
data class UpdateUserProfileCommand(
    val userId: UUID,
    val username: String
)
