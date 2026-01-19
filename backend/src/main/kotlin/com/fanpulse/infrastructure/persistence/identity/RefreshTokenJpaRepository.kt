package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.RefreshTokenRecord
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Component
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

    @Column(name = "token", nullable = false, unique = true)
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

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.invalidated = true WHERE r.token = :token")
    fun invalidateByToken(token: String): Int

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.invalidated = true WHERE r.userId = :userId")
    fun invalidateAllByUserId(userId: UUID): Int

    @Modifying
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
