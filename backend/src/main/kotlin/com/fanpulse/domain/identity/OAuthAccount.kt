package com.fanpulse.domain.identity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * OAuth account entity - linked to a User aggregate.
 * Represents an external social login account (e.g., Google).
 */
@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_oauth_accounts_provider_user",
            columnNames = ["provider", "provider_user_id"]
        )
    ]
)
class OAuthAccount(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val provider: OAuthProvider,

    @Column(name = "provider_user_id", length = 255, nullable = false)
    val providerUserId: String,

    @Column(length = 100)
    val email: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    companion object {
        fun create(
            userId: UUID,
            provider: OAuthProvider,
            providerUserId: String,
            email: String? = null
        ): OAuthAccount = OAuthAccount(
            userId = userId,
            provider = provider,
            providerUserId = providerUserId,
            email = email
        )
    }
}
