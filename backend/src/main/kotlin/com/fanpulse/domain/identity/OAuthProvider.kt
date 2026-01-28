package com.fanpulse.domain.identity

/**
 * Supported OAuth providers.
 */
enum class OAuthProvider {
    GOOGLE;

    companion object {
        fun fromString(value: String): OAuthProvider =
            entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown OAuth provider: $value")
    }
}
