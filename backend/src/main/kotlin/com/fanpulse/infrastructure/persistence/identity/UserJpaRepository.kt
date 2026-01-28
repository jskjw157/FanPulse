package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.port.UserPort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * JPA Repository interface for User entity.
 * Extends JpaRepository for standard CRUD operations.
 */
interface UserJpaRepositoryInterface : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByUsername(username: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}

/**
 * UserPort implementation using Spring Data JPA.
 * Acts as an adapter between the domain layer and JPA.
 */
@Repository
class UserJpaRepository(
    private val jpaRepository: UserJpaRepositoryInterface
) : UserPort {

    override fun findById(id: UUID): User? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return jpaRepository.findByEmail(email)
    }

    override fun findByUsername(username: String): User? {
        return jpaRepository.findByUsername(username)
    }

    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }

    override fun existsByUsername(username: String): Boolean {
        return jpaRepository.existsByUsername(username)
    }

    override fun save(user: User): User {
        return jpaRepository.save(user)
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
