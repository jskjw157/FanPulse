package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.port.UserPort
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserPort {

    override fun save(user: User): User {
        val entity = UserMapper.toEntity(user)
        val savedEntity = userJpaRepository.save(entity)
        return UserMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): User? {
        return userJpaRepository.findById(id)
            .map { UserMapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)
            ?.let { UserMapper.toDomain(it) }
    }

    override fun findByUsername(username: String): User? {
        return userJpaRepository.findByUsername(username)
            ?.let { UserMapper.toDomain(it) }
    }

    override fun existsByUsername(username: String): Boolean {
        return userJpaRepository.existsByUsername(username)
    }

    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmail(email)
    }

    override fun findMaxUsernameSuffix(prefix: String): Int {
        return userJpaRepository.findMaxUsernameSuffix(prefix)
    }
}
