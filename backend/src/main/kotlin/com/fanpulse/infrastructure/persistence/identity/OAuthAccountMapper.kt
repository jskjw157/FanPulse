package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.OAuthAccount
import com.fanpulse.domain.identity.OAuthProvider

object OAuthAccountMapper {
    fun toDomain(entity: OAuthAccountEntity): OAuthAccount {
        return OAuthAccount(
            id = entity.id,
            userId = entity.userId,
            provider = OAuthProvider.fromString(entity.provider),
            providerUserId = entity.providerUserId,
            email = entity.email,
            emailVerified = entity.emailVerified,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: OAuthAccount): OAuthAccountEntity {
        return OAuthAccountEntity(
            id = domain.id,
            userId = domain.userId,
            provider = domain.provider.name,
            providerUserId = domain.providerUserId,
            email = domain.email,
            emailVerified = domain.emailVerified,
            createdAt = domain.createdAt
        )
    }
}
