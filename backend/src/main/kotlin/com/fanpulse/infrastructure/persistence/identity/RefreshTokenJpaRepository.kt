package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.RefreshTokenRecord
import com.fanpulse.domain.identity.port.TokenInvalidationResult
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

/**
 * Refresh Token JPA Entity
 * auth_tokens 테이블 매핑
 */
@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "token", nullable = false, unique = true, length = 512)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,

    @Column(name = "invalidated", nullable = false)
    var invalidated: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    fun toRecord(): RefreshTokenRecord = RefreshTokenRecord(
        id = id,
        userId = userId,
        token = token,
        expiresAt = expiresAt,
        invalidated = invalidated,
        createdAt = createdAt
    )
}

/**
 * Spring Data JPA Repository
 */
@Repository
interface RefreshTokenJpaRepositoryInterface : JpaRepository<RefreshTokenEntity, UUID> {

    fun findByToken(token: String): RefreshTokenEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.invalidated = true WHERE r.token = :token")
    fun invalidateByToken(token: String): Int

    /**
     * CAS(Compare-And-Swap) 패턴: 활성 토큰만 원자적으로 무효화.
     * WHERE 조건에 invalidated = false를 포함하여 동시 요청 시 하나만 성공한다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.invalidated = true WHERE r.token = :token AND r.invalidated = false")
    fun casInvalidateByToken(token: String): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.invalidated = true WHERE r.userId = :userId")
    fun invalidateAllByUserId(userId: UUID): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < :now")
    fun deleteExpiredBefore(now: Instant): Int
}

/**
 * RefreshTokenPort 구현체 (Adapter)
 */
@Component
class RefreshTokenAdapter(
    private val repository: RefreshTokenJpaRepositoryInterface
) : RefreshTokenPort {

    override fun save(userId: UUID, token: String, expiresAt: Instant) {
        val entity = RefreshTokenEntity(
            userId = userId,
            token = token,
            expiresAt = expiresAt
        )
        repository.save(entity)
    }

    override fun findByToken(token: String): RefreshTokenRecord? {
        return repository.findByToken(token)?.toRecord()
    }

    @Transactional
    override fun findAndInvalidateByToken(token: String): TokenInvalidationResult {
        val updated = repository.casInvalidateByToken(token)
        if (updated > 0) {
            return TokenInvalidationResult.Invalidated
        }
        val existing = repository.findByToken(token)
        return if (existing != null && existing.invalidated) {
            TokenInvalidationResult.AlreadyInvalidated
        } else {
            TokenInvalidationResult.NotFound
        }
    }

    override fun invalidate(token: String) {
        repository.invalidateByToken(token)
    }

    override fun invalidateAllByUserId(userId: UUID) {
        repository.invalidateAllByUserId(userId)
    }

    override fun deleteExpiredTokens(): Int {
        return repository.deleteExpiredBefore(Instant.now())
    }
}
