package com.fanpulse.infrastructure.web.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.application.service.identity.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * Controller for authenticated user's profile and settings.
 *
 * Note: In production, userId should be extracted from JWT token via SecurityContext.
 * Current implementation uses @RequestAttribute which should be set by JwtAuthenticationFilter.
 */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "User Profile", description = "Current user profile and settings management")
@SecurityRequirement(name = "bearerAuth")
class MeController(
    private val userService: UserService
) {

    @GetMapping
    @Operation(
        summary = "Get current user profile",
        description = "Returns the authenticated user's profile information"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully",
            content = [Content(schema = Schema(implementation = UserResponse::class))]
        ),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun getMe(
        @Parameter(hidden = true)
        @RequestAttribute("userId") userId: UUID
    ): ResponseEntity<UserResponse> {
        val user = userService.getUser(userId)
        return ResponseEntity.ok(user)
    }

    @PatchMapping
    @Operation(
        summary = "Update current user profile",
        description = "Updates the authenticated user's profile information"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = [Content(schema = Schema(implementation = UserResponse::class))]
        ),
        ApiResponse(responseCode = "400", description = "Invalid request or username taken"),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun updateMe(
        @Parameter(hidden = true)
        @RequestAttribute("userId") userId: UUID,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.updateProfile(userId, request)
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/password")
    @Operation(
        summary = "Change password",
        description = "Changes the authenticated user's password"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Password changed successfully"),
        ApiResponse(responseCode = "400", description = "Invalid current password or weak new password"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
        ApiResponse(responseCode = "409", description = "User is OAuth-only (no password)")
    )
    fun changePassword(
        @Parameter(hidden = true)
        @RequestAttribute("userId") userId: UUID,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<MessageResponse> {
        userService.changePassword(userId, request)
        return ResponseEntity.ok(MessageResponse("Password changed successfully"))
    }

    @GetMapping("/settings")
    @Operation(
        summary = "Get user settings",
        description = "Returns the authenticated user's settings"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Settings retrieved successfully",
            content = [Content(schema = Schema(implementation = UserSettingsResponse::class))]
        ),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun getSettings(
        @Parameter(hidden = true)
        @RequestAttribute("userId") userId: UUID
    ): ResponseEntity<UserSettingsResponse> {
        val settings = userService.getSettings(userId)
        return ResponseEntity.ok(settings)
    }

    @PatchMapping("/settings")
    @Operation(
        summary = "Update user settings",
        description = "Updates the authenticated user's settings"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Settings updated successfully",
            content = [Content(schema = Schema(implementation = UserSettingsResponse::class))]
        ),
        ApiResponse(responseCode = "400", description = "Invalid settings values"),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun updateSettings(
        @Parameter(hidden = true)
        @RequestAttribute("userId") userId: UUID,
        @Valid @RequestBody request: UpdateSettingsRequest
    ): ResponseEntity<UserSettingsResponse> {
        val settings = userService.updateSettings(userId, request)
        return ResponseEntity.ok(settings)
    }
}
