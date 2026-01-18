package com.fanpulse.infrastructure.persistence.identity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OAuthAccountJpaRepository : JpaRepository<OAuthAccountEntity, UUID> {
    fun findByProviderAndProviderUserId(provider: String, providerUserId: String): OAuthAccountEntity?
    fun findByUserId(userId: UUID): List<OAuthAccountEntity>
}
