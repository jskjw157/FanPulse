package com.fanpulse.domain.identity.port

/**
 * Domain Port for OAuth token verification.
 * Infrastructure adapters implement this for specific providers (Google, Apple, etc.)
 */
interface OAuthTokenVerifierPort {
    /**
     * Verifies the OAuth token and returns user information.
     *
     * @param idToken The ID token from the OAuth provider
     * @return User information extracted from the token, or null if verification fails
     */
    fun verify(idToken: String): OAuthUserInfo?
}

/**
 * User information extracted from OAuth provider.
 */
data class OAuthUserInfo(
    val providerUserId: String,
    val email: String,
    val emailVerified: Boolean,
    val name: String?,
    val pictureUrl: String?
)
