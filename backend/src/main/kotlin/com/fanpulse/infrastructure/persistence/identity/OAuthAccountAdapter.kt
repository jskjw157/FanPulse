package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.OAuthAccount
import com.fanpulse.domain.identity.OAuthProvider
import com.fanpulse.domain.identity.port.OAuthAccountPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class OAuthAccountAdapter(
    private val oAuthAccountJpaRepository: OAuthAccountJpaRepository
) : OAuthAccountPort {

    override fun save(oAuthAccount: OAuthAccount): OAuthAccount {
        val entity = OAuthAccountMapper.toEntity(oAuthAccount)
        val savedEntity = oAuthAccountJpaRepository.save(entity)
        return OAuthAccountMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): OAuthAccount? {
        return oAuthAccountJpaRepository.findById(id)
            .map { OAuthAccountMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String
    ): OAuthAccount? {
        return oAuthAccountJpaRepository.findByProviderAndProviderUserId(
            provider = provider.name,
            providerUserId = providerUserId
        )?.let { OAuthAccountMapper.toDomain(it) }
    }

    override fun findByUserId(userId: UUID): List<OAuthAccount> {
        return oAuthAccountJpaRepository.findByUserId(userId)
            .map { OAuthAccountMapper.toDomain(it) }
    }

    override fun saveIfNotExists(oAuthAccount: OAuthAccount): OAuthAccount? {
        return try {
            val entity = OAuthAccountMapper.toEntity(oAuthAccount)
            val savedEntity = oAuthAccountJpaRepository.save(entity)
            OAuthAccountMapper.toDomain(savedEntity)
        } catch (e: DataIntegrityViolationException) {
            // UNIQUE 제약조건 위반 (이미 존재하는 OAuth 계정)
            null
        }
    }
}
