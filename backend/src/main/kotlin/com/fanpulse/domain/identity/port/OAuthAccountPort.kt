package com.fanpulse.domain.identity.port

import com.fanpulse.domain.identity.OAuthAccount
import com.fanpulse.domain.identity.OAuthProvider
import java.util.UUID

/**
 * Domain Port for OAuthAccount persistence.
 */
interface OAuthAccountPort {

    /**
     * Finds an OAuth account by provider and provider user ID.
     */
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): OAuthAccount?

    /**
     * Finds all OAuth accounts for a user.
     */
    fun findByUserId(userId: UUID): List<OAuthAccount>

    /**
     * Saves an OAuth account.
     */
    fun save(account: OAuthAccount): OAuthAccount
}
