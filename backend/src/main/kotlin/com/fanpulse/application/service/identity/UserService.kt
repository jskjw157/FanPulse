package com.fanpulse.application.service.identity

import com.fanpulse.application.dto.identity.*
import java.util.UUID

/**
 * User service interface.
 * Handles user profile and settings management.
 */
interface UserService {

    /**
     * Gets a user's profile by ID.
     * @throws NoSuchElementException if user not found
     */
    fun getUser(userId: UUID): UserResponse

    /**
     * Updates a user's profile.
     * @throws NoSuchElementException if user not found
     * @throws IllegalArgumentException if username already taken
     */
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserResponse

    /**
     * Changes a user's password.
     * @throws NoSuchElementException if user not found
     * @throws IllegalStateException if user is OAuth-only
     * @throws IllegalArgumentException if current password is wrong
     */
    fun changePassword(userId: UUID, request: ChangePasswordRequest)

    /**
     * Gets a user's settings.
     * @throws NoSuchElementException if user not found
     */
    fun getSettings(userId: UUID): UserSettingsResponse

    /**
     * Updates a user's settings.
     * @throws NoSuchElementException if user not found
     */
    fun updateSettings(userId: UUID, request: UpdateSettingsRequest): UserSettingsResponse
}
