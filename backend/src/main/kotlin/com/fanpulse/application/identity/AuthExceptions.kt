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

/**
 * Refresh Token 재사용 감지 예외
 * 이미 무효화된 토큰이 다시 사용되면 발생 (보안 침해 가능성)
 */
class RefreshTokenReusedException(
    message: String = "Refresh token has already been used. All tokens invalidated for security."
) : AuthException(message)

/**
 * Rate Limit 초과 예외
 */
class RateLimitExceededException(
    val retryAfterSeconds: Long,
    message: String = "Too many requests. Please try again later."
) : AuthException(message)
