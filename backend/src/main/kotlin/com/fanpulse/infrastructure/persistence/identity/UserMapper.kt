package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username

object UserMapper {
    fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id,
            username = Username.of(entity.username),
            email = Email.of(entity.email),
            passwordHash = entity.passwordHash,
            emailVerified = entity.emailVerified,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: User): UserEntity {
        return UserEntity(
            id = domain.id,
            username = domain.username.value,
            email = domain.email.value,
            passwordHash = domain.passwordHash,
            emailVerified = domain.emailVerified,
            createdAt = domain.createdAt
        )
    }
}
