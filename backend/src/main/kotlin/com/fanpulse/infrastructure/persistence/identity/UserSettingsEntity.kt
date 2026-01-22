package com.fanpulse.infrastructure.persistence.identity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_settings")
data class UserSettingsEntity(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false, unique = true)
    val userId: UUID,

    @Column(name = "theme", nullable = false, length = 10)
    val theme: String = "light",

    @Column(name = "language", nullable = false, length = 10)
    val language: String = "ko",

    @Column(name = "push_enabled", nullable = false)
    val pushEnabled: Boolean = true,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)
