package com.fanpulse.domain.identity

/**
 * OAuth 제공자 열거형
 */
enum class OAuthProvider {
    GOOGLE;

    companion object {
        fun fromString(value: String): OAuthProvider {
            return valueOf(value.uppercase())
        }
    }
}
