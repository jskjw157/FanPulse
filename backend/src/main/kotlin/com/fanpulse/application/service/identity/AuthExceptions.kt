package com.fanpulse.application.service.identity

/**
 * 인증 관련 예외 베이스 클래스
 */
abstract class AuthException(message: String) : RuntimeException(message)

/**
 * 이메일 중복 예외
 */
class EmailAlreadyExistsException(
    email: String
) : AuthException("Email already exists: $email")

/**
 * 사용자명 중복 예외
 */
class UsernameAlreadyExistsException(
    username: String
) : AuthException("Username already exists: $username")

/**
 * 잘못된 인증 정보 예외
 */
class InvalidCredentialsException(
    message: String = "Invalid email or password"
) : AuthException(message)

/**
 * 유효하지 않은 Google 토큰 예외
 */
class InvalidGoogleTokenException(
    message: String = "Invalid or expired Google token"
) : AuthException(message)

/**
 * 사용자를 찾을 수 없음 예외
 */
class UserNotFoundException(
    message: String = "User not found"
) : AuthException(message)

/**
 * 유효하지 않은 토큰 예외
 */
class InvalidTokenException(
    message: String = "Invalid or expired token"
) : AuthException(message)

/**
 * 이메일 미검증 계정에 OAuth 연결 시도 예외
 */
class EmailNotVerifiedException(
    email: String
) : AuthException("Cannot link OAuth to unverified email account: $email. Please verify your email first.")

/**
 * OAuth 이메일 미검증 예외
 */
class OAuthEmailNotVerifiedException(
    provider: String
) : AuthException("Email from $provider is not verified")

/**
 * OAuth 계정 중복 생성 시도 예외 (Race Condition)
 */
class OAuthAccountAlreadyExistsException(
    provider: String,
    providerUserId: String
) : AuthException("OAuth account already exists: $provider:$providerUserId")

/**
 * OAuth 계정을 찾을 수 없음 예외
 */
class OAuthAccountNotFoundException(
    message: String = "OAuth account not found"
) : AuthException(message)
