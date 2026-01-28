package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.util.*

/**
 * OAuthAccountJpaRepository TDD Tests
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OAuthAccountJpaRepository")
class OAuthAccountJpaRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var jpaRepository: OAuthAccountJpaRepositoryInterface

    private lateinit var oAuthAccountRepository: OAuthAccountJpaRepository

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepositoryInterface

    private lateinit var savedUser: User

    @BeforeEach
    fun setUp() {
        oAuthAccountRepository = OAuthAccountJpaRepository(jpaRepository)

        // Create a user first (foreign key constraint)
        savedUser = userJpaRepository.save(
            User.registerWithOAuth(
                email = Email.of("oauth@example.com"),
                username = Username.of("oauthuser")
            )
        )
        entityManager.flush()
    }

    @Nested
    @DisplayName("OAuth 계정 저장 및 조회")
    inner class SaveAndFind {

        @Test
        @DisplayName("OAuth 계정을 저장하고 provider와 providerUserId로 조회할 수 있어야 한다")
        fun `should save and find by provider and providerUserId`() {
            // Given
            val oAuthAccount = OAuthAccount.create(
                userId = savedUser.id,
                provider = OAuthProvider.GOOGLE,
                providerUserId = "google-12345",
                email = "oauth@gmail.com"
            )

            // When
            val savedAccount = oAuthAccountRepository.save(oAuthAccount)
            entityManager.flush()
            entityManager.clear()
            val foundAccount = oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                "google-12345"
            )

            // Then
            assertNotNull(foundAccount, "OAuth account should be found")
            assertEquals(savedAccount.id, foundAccount?.id)
            assertEquals(OAuthProvider.GOOGLE, foundAccount?.provider)
            assertEquals("google-12345", foundAccount?.providerUserId)
            assertEquals("oauth@gmail.com", foundAccount?.email)
        }

        @Test
        @DisplayName("존재하지 않는 provider/providerUserId 조합은 null을 반환해야 한다")
        fun `should return null for non-existent provider combination`() {
            // When
            val foundAccount = oAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                "non-existent"
            )

            // Then
            assertNull(foundAccount, "OAuth account should not be found")
        }
    }

    @Nested
    @DisplayName("사용자별 OAuth 계정 조회")
    inner class FindByUserId {

        @Test
        @DisplayName("userId로 해당 사용자의 OAuth 계정을 조회할 수 있어야 한다")
        fun `should find all OAuth accounts by userId`() {
            // Given
            val googleAccount = OAuthAccount.create(
                userId = savedUser.id,
                provider = OAuthProvider.GOOGLE,
                providerUserId = "google-12345"
            )
            oAuthAccountRepository.save(googleAccount)
            entityManager.flush()
            entityManager.clear()

            // When
            val accounts = oAuthAccountRepository.findByUserId(savedUser.id)

            // Then
            assertEquals(1, accounts.size, "Should find 1 OAuth account")
            assertTrue(
                accounts.any { it.provider == OAuthProvider.GOOGLE },
                "Should have Google account"
            )
        }

        @Test
        @DisplayName("OAuth 계정이 없는 사용자는 빈 리스트를 반환해야 한다")
        fun `should return empty list for user with no OAuth accounts`() {
            // When
            val accounts = oAuthAccountRepository.findByUserId(savedUser.id)

            // Then
            assertTrue(accounts.isEmpty(), "Should return empty list")
        }
    }
}
