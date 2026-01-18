package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.util.*

/**
 * UserJpaRepository TDD Tests
 *
 * RED Phase: Repository 인터페이스가 없어서 컴파일 실패해야 함
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserJpaRepository")
class UserJpaRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var jpaRepository: UserJpaRepositoryInterface

    private lateinit var userRepository: UserJpaRepository

    @BeforeEach
    fun setUp() {
        userRepository = UserJpaRepository(jpaRepository)
    }

    @Nested
    @DisplayName("사용자 저장 및 조회")
    inner class SaveAndFind {

        @Test
        @DisplayName("사용자를 저장하고 ID로 조회할 수 있어야 한다")
        fun `should save and find user by id`() {
            // Given
            val user = User.register(
                email = Email.of("test@example.com"),
                username = Username.of("testuser"),
                encodedPassword = "encoded_password_hash"
            )

            // When
            val savedUser = userRepository.save(user)
            entityManager.flush()
            entityManager.clear()
            val foundUser = userRepository.findById(savedUser.id)

            // Then
            assertNotNull(foundUser)
            assertEquals(savedUser.id, foundUser?.id)
            assertEquals("test@example.com", foundUser?.email)
            assertEquals("testuser", foundUser?.username)
        }

        @Test
        @DisplayName("이메일로 사용자를 조회할 수 있어야 한다")
        fun `should find user by email`() {
            // Given
            val user = User.register(
                email = Email.of("findme@example.com"),
                username = Username.of("findmeuser"),
                encodedPassword = "encoded_password_hash"
            )
            userRepository.save(user)
            entityManager.flush()
            entityManager.clear()

            // When
            val foundUser = userRepository.findByEmail("findme@example.com")

            // Then
            assertNotNull(foundUser)
            assertEquals("findme@example.com", foundUser?.email)
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회하면 null을 반환해야 한다")
        fun `should return null for non-existent email`() {
            // When
            val foundUser = userRepository.findByEmail("nonexistent@example.com")

            // Then
            assertNull(foundUser)
        }

        @Test
        @DisplayName("유저네임으로 사용자를 조회할 수 있어야 한다")
        fun `should find user by username`() {
            // Given
            val user = User.register(
                email = Email.of("byusername@example.com"),
                username = Username.of("uniqueusername"),
                encodedPassword = "encoded_password_hash"
            )
            userRepository.save(user)
            entityManager.flush()
            entityManager.clear()

            // When
            val foundUser = userRepository.findByUsername("uniqueusername")

            // Then
            assertNotNull(foundUser)
            assertEquals("uniqueusername", foundUser?.username)
        }
    }

    @Nested
    @DisplayName("존재 여부 확인")
    inner class ExistsChecks {

        @Test
        @DisplayName("이메일 존재 여부를 확인할 수 있어야 한다")
        fun `should check if email exists`() {
            // Given
            val user = User.register(
                email = Email.of("exists@example.com"),
                username = Username.of("existsuser"),
                encodedPassword = "encoded_password_hash"
            )
            userRepository.save(user)
            entityManager.flush()

            // When & Then
            assertTrue(userRepository.existsByEmail("exists@example.com"), "Email should exist")
            assertFalse(userRepository.existsByEmail("notexists@example.com"), "Email should not exist")
        }

        @Test
        @DisplayName("유저네임 존재 여부를 확인할 수 있어야 한다")
        fun `should check if username exists`() {
            // Given
            val user = User.register(
                email = Email.of("checkusername@example.com"),
                username = Username.of("existingname"),
                encodedPassword = "encoded_password_hash"
            )
            userRepository.save(user)
            entityManager.flush()

            // When & Then
            assertTrue(userRepository.existsByUsername("existingname"), "Username should exist")
            assertFalse(userRepository.existsByUsername("nonexistingname"), "Username should not exist")
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    inner class DeleteUser {

        @Test
        @DisplayName("사용자를 ID로 삭제할 수 있어야 한다")
        fun `should delete user by id`() {
            // Given
            val user = User.register(
                email = Email.of("todelete@example.com"),
                username = Username.of("todeleteuser"),
                encodedPassword = "encoded_password_hash"
            )
            val savedUser = userRepository.save(user)
            entityManager.flush()

            // When
            userRepository.deleteById(savedUser.id)
            entityManager.flush()
            entityManager.clear()

            // Then
            val deletedUser = userRepository.findById(savedUser.id)
            assertNull(deletedUser)
        }
    }
}
