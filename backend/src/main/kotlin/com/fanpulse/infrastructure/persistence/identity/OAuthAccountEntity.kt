package com.fanpulse.infrastructure.persistence.identity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "provider_user_id"])
    ]
)
data class OAuthAccountEntity(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "provider", nullable = false, length = 20)
    val provider: String,

    @Column(name = "provider_user_id", nullable = false, length = 255)
    val providerUserId: String,

    @Column(name = "email", length = 100)
    val email: String,

    @Column(name = "email_verified", nullable = false)
    val emailVerified: Boolean = false,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
