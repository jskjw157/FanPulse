package com.fanpulse.application.identity

/**
 * Authentication related exceptions
 */
open class AuthException(message: String) : RuntimeException(message)

class EmailAlreadyExistsException(
    val email: String
) : AuthException("Email already exists: $email")

class UsernameAlreadyExistsException(
    val username: String
) : AuthException("Username already exists: $username")

class InvalidCredentialsException : AuthException("Invalid email or password")

class InvalidTokenException(
    message: String = "Invalid or expired token"
) : AuthException(message)

class UserNotFoundException(
    message: String = "User not found"
) : AuthException(message)

class InvalidPasswordException(
    message: String = "Current password is incorrect"
) : AuthException(message)
