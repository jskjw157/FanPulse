package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.OAuthAccount
import com.fanpulse.domain.identity.OAuthProvider
import com.fanpulse.domain.identity.port.OAuthAccountPort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * JPA Repository interface for OAuthAccount entity.
 */
interface OAuthAccountJpaRepositoryInterface : JpaRepository<OAuthAccount, UUID> {
    fun findByProviderAndProviderUserId(provider: OAuthProvider, providerUserId: String): OAuthAccount?
    fun findByUserId(userId: UUID): List<OAuthAccount>
}

/**
 * OAuthAccountPort implementation using Spring Data JPA.
 */
@Repository
class OAuthAccountJpaRepository(
    private val jpaRepository: OAuthAccountJpaRepositoryInterface
) : OAuthAccountPort {

    override fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String
    ): OAuthAccount? {
        return jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
    }

    override fun findByUserId(userId: UUID): List<OAuthAccount> {
        return jpaRepository.findByUserId(userId)
    }

    override fun save(account: OAuthAccount): OAuthAccount {
        return jpaRepository.save(account)
    }
}
