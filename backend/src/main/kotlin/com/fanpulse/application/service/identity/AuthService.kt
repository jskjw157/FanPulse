package com.fanpulse.application.service.identity

import com.fanpulse.application.dto.identity.*

/**
 * Authentication service interface.
 * Handles user registration, login, and token management.
 */
interface AuthService {

    /**
     * Registers a new user with email/password.
     * @throws IllegalArgumentException if email/username already exists
     */
    fun signup(request: SignupRequest): TokenResponse

    /**
     * Authenticates a user with email/password.
     * @throws IllegalArgumentException if credentials are invalid
     */
    fun login(request: LoginRequest): TokenResponse

    /**
     * Authenticates a user with Google OAuth.
     * Creates a new user if not exists.
     */
    fun googleLogin(request: GoogleLoginRequest): TokenResponse

    /**
     * Refreshes an access token using a refresh token.
     * @throws IllegalArgumentException if refresh token is invalid or expired
     */
    fun refreshToken(request: RefreshTokenRequest): TokenResponse

    /**
     * Logs out a user by invalidating their tokens.
     * @param accessToken The current access token
     */
    fun logout(accessToken: String)
}
