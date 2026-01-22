#!/bin/bash

# Script to generate Auth infrastructure code
# This follows the Script-First Principle to save tokens

BASE_DIR="/Users/ohchaeeun/source/feature-125/FanPulse/backend/src/main/kotlin/com/fanpulse"

echo "Generating Auth infrastructure code..."
echo "Summary will be output as JSON at the end"

# Counter for created files
CREATED_COUNT=0

# Create JPA Entity: UserEntity
cat > "$BASE_DIR/infrastructure/persistence/identity/UserEntity.kt" <<'EOF'
package com.fanpulse.infrastructure.persistence.identity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "username", nullable = false, unique = true, length = 50)
    val username: String,

    @Column(name = "email", nullable = false, unique = true, length = 100)
    val email: String,

    @Column(name = "password_hash", columnDefinition = "TEXT")
    val passwordHash: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
EOF
((CREATED_COUNT++))
echo "Created: UserEntity.kt"

# Create JPA Entity: OAuthAccountEntity
cat > "$BASE_DIR/infrastructure/persistence/identity/OAuthAccountEntity.kt" <<'EOF'
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

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
EOF
((CREATED_COUNT++))
echo "Created: OAuthAccountEntity.kt"

# Create JPA Entity: UserSettingsEntity
cat > "$BASE_DIR/infrastructure/persistence/identity/UserSettingsEntity.kt" <<'EOF'
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
EOF
((CREATED_COUNT++))
echo "Created: UserSettingsEntity.kt"

# Create JPA Repository: UserJpaRepository
cat > "$BASE_DIR/infrastructure/persistence/identity/UserJpaRepository.kt" <<'EOF'
package com.fanpulse.infrastructure.persistence.identity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun findByUsername(username: String): UserEntity?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
EOF
((CREATED_COUNT++))
echo "Created: UserJpaRepository.kt"

# Create JPA Repository: OAuthAccountJpaRepository
cat > "$BASE_DIR/infrastructure/persistence/identity/OAuthAccountJpaRepository.kt" <<'EOF'
package com.fanpulse.infrastructure.persistence.identity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OAuthAccountJpaRepository : JpaRepository<OAuthAccountEntity, UUID> {
    fun findByProviderAndProviderUserId(provider: String, providerUserId: String): OAuthAccountEntity?
    fun findByUserId(userId: UUID): List<OAuthAccountEntity>
}
EOF
((CREATED_COUNT++))
echo "Created: OAuthAccountJpaRepository.kt"

# Create JPA Repository: UserSettingsJpaRepository
cat > "$BASE_DIR/infrastructure/persistence/identity/UserSettingsJpaRepository.kt" <<'EOF'
package com.fanpulse.infrastructure.persistence.identity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserSettingsJpaRepository : JpaRepository<UserSettingsEntity, UUID> {
    fun findByUserId(userId: UUID): UserSettingsEntity?
}
EOF
((CREATED_COUNT++))
echo "Created: UserSettingsJpaRepository.kt"

echo ""
echo "=== Generation Complete ==="
echo "{\"created\": $CREATED_COUNT, \"failed\": 0}"
