package com.fanpulse.application.dto.identity

import com.fanpulse.domain.identity.Language
import com.fanpulse.domain.identity.Theme
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.UserSettings
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

// === Response DTOs ===

@Schema(description = "User profile response")
data class UserResponse(
    @Schema(description = "User ID")
    val id: UUID,

    @Schema(description = "Email address")
    val email: String,

    @Schema(description = "Display name")
    val username: String,

    @Schema(description = "Whether user has password (false = OAuth only)")
    val hasPassword: Boolean,

    @Schema(description = "Account creation timestamp")
    val createdAt: Instant
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id,
            email = user.email,
            username = user.username,
            hasPassword = user.hasPassword(),
            createdAt = user.createdAt
        )
    }
}

@Schema(description = "User settings response")
data class UserSettingsResponse(
    @Schema(description = "Theme (LIGHT/DARK)")
    val theme: String,

    @Schema(description = "Language (KO/EN)")
    val language: String,

    @Schema(description = "Push notification enabled")
    val pushEnabled: Boolean,

    @Schema(description = "Last updated timestamp")
    val updatedAt: Instant
) {
    companion object {
        fun from(settings: UserSettings): UserSettingsResponse = UserSettingsResponse(
            theme = settings.theme.name.lowercase(),
            language = settings.language.name.lowercase(),
            pushEnabled = settings.pushEnabled,
            updatedAt = settings.updatedAt
        )
    }
}

// === Request DTOs ===

@Schema(description = "Profile update request")
data class UpdateProfileRequest(
    @field:Size(min = 2, max = 50, message = "Username must be 2-50 characters")
    @Schema(description = "New display name", example = "new_username")
    val username: String? = null
)

@Schema(description = "Settings update request")
data class UpdateSettingsRequest(
    @Schema(description = "Theme (light/dark)", example = "dark")
    val theme: String? = null,

    @Schema(description = "Language (ko/en)", example = "ko")
    val language: String? = null,

    @Schema(description = "Push notification enabled", example = "true")
    val pushEnabled: Boolean? = null
) {
    fun toTheme(): Theme? = theme?.let { Theme.fromString(it) }
    fun toLanguage(): Language? = language?.let { Language.fromString(it) }
}
