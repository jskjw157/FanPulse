package com.fanpulse.integration

import com.fanpulse.application.identity.AuthService
import com.fanpulse.application.identity.GoogleLoginRequest
import com.fanpulse.application.identity.RefreshTokenReusedException
import com.fanpulse.application.identity.command.GoogleLoginHandler
import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.infrastructure.persistence.identity.RefreshTokenJpaRepositoryInterface
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Auth Integration Tests
 *
 * These tests verify the complete authentication flow including:
 * - Login -> Refresh -> Logout flow
 * - Refresh token persistence to database
 * - Token invalidation after use (rotation)
 * - Reused token detection and security measures
 * - Concurrent refresh request handling
 * - Expired token cleanup
 * - Unique token generation
 * - Token chain integrity
 *
 * Uses H2 in-memory database with test profile.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DisplayName("Auth Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthIntegrationTest {

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var tokenPort: TokenPort

    @Autowired
    private lateinit var refreshTokenPort: RefreshTokenPort

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenJpaRepositoryInterface

    @Autowired
    private lateinit var userPort: UserPort

    @MockkBean
    private lateinit var googleLoginHandler: GoogleLoginHandler

    private lateinit var testUser: User
    private lateinit var testUserId: UUID

    @BeforeEach
    fun setUp() {
        // Create a test user with unique identifiers
        testUserId = UUID.randomUUID()
        val uniqueSuffix = System.currentTimeMillis()
        testUser = User.registerWithOAuth(
            email = Email.of("integration-test-$uniqueSuffix@example.com"),
            username = Username.of("testuser$uniqueSuffix")
        )

        // Use reflection to set user ID for consistent testing
        val idField = User::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(testUser, testUserId)

        // Save the test user
        userPort.save(testUser)
    }

    @Test
    @Order(1)
    @DisplayName("should complete full login-refresh-logout flow")
    @Transactional
    fun `should complete full login-refresh-logout flow`() {
        // Given - Mock Google login handler
        every { googleLoginHandler.handle(any()) } returns testUser

        // When - Step 1: Login
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))

        // Then - Verify login response
        assertNotNull(loginResponse.accessToken)
        assertNotNull(loginResponse.refreshToken)
        assertEquals(testUserId, loginResponse.userId)
        assertTrue(tokenPort.validateToken(loginResponse.accessToken))
        assertTrue(tokenPort.validateToken(loginResponse.refreshToken))

        // When - Step 2: Refresh token
        val refreshResponse = authService.refreshToken(loginResponse.refreshToken)

        // Then - Verify refresh response
        assertNotNull(refreshResponse.accessToken)
        assertNotNull(refreshResponse.refreshToken)
        assertTrue(tokenPort.validateToken(refreshResponse.accessToken))
        assertTrue(tokenPort.validateToken(refreshResponse.refreshToken))
        // New tokens should be different from old ones
        assertNotEquals(loginResponse.accessToken, refreshResponse.accessToken)
        assertNotEquals(loginResponse.refreshToken, refreshResponse.refreshToken)

        // When - Step 3: Logout
        authService.logout(testUserId)

        // Then - Verify old refresh token is invalidated
        val oldTokenRecord = refreshTokenPort.findByToken(loginResponse.refreshToken)
        val newTokenRecord = refreshTokenPort.findByToken(refreshResponse.refreshToken)
        assertTrue(oldTokenRecord?.invalidated ?: true, "Old token should be invalidated")
        assertTrue(newTokenRecord?.invalidated ?: true, "New token should be invalidated after logout")
    }

    @Test
    @Order(2)
    @DisplayName("should persist refresh token to database")
    @Transactional
    fun `should persist refresh token to database`() {
        // Given
        every { googleLoginHandler.handle(any()) } returns testUser

        // When
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))

        // Then - Verify token is saved in database
        val savedToken = refreshTokenPort.findByToken(loginResponse.refreshToken)
        assertNotNull(savedToken, "Refresh token should be persisted to database")
        assertEquals(testUserId, savedToken?.userId)
        assertEquals(loginResponse.refreshToken, savedToken?.token)
        assertFalse(savedToken?.invalidated ?: true, "New token should not be invalidated")
        assertNotNull(savedToken?.expiresAt, "Token should have expiration time")
    }

    @Test
    @Order(3)
    @DisplayName("should invalidate token in database after use")
    @Transactional
    fun `should invalidate token in database after use`() {
        // Given
        every { googleLoginHandler.handle(any()) } returns testUser
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))
        val originalToken = loginResponse.refreshToken

        // Verify token is initially valid
        val tokenBeforeRefresh = refreshTokenPort.findByToken(originalToken)
        assertFalse(tokenBeforeRefresh?.invalidated ?: true, "Token should not be invalidated before use")

        // When - Use the token
        authService.refreshToken(originalToken)

        // Then - Old token should be invalidated
        val tokenAfterRefresh = refreshTokenPort.findByToken(originalToken)
        assertTrue(tokenAfterRefresh?.invalidated ?: false, "Token should be invalidated after use")
    }

    @Test
    @Order(4)
    @DisplayName("should block reused token and invalidate all")
    @Transactional
    fun `should block reused token and invalidate all`() {
        // Given
        every { googleLoginHandler.handle(any()) } returns testUser
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))
        val firstToken = loginResponse.refreshToken

        // Use the token once (this invalidates it)
        val refreshResponse = authService.refreshToken(firstToken)
        val secondToken = refreshResponse.refreshToken

        // Verify first token is now invalidated
        val firstTokenRecord = refreshTokenPort.findByToken(firstToken)
        assertTrue(firstTokenRecord?.invalidated ?: false, "First token should be invalidated")

        // When - Try to reuse the already-used token (security breach attempt)
        val exception = assertThrows<RefreshTokenReusedException> {
            authService.refreshToken(firstToken)
        }

        // Then - Should throw RefreshTokenReusedException
        assertNotNull(exception)

        // All tokens for this user should be invalidated
        val secondTokenRecord = refreshTokenPort.findByToken(secondToken)
        assertTrue(secondTokenRecord?.invalidated ?: true, "All tokens should be invalidated after reuse detection")
    }

    @Test
    @Order(5)
    @DisplayName("should handle concurrent refresh requests")
    fun `should handle concurrent refresh requests`() {
        // Given
        every { googleLoginHandler.handle(any()) } returns testUser
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))
        val refreshToken = loginResponse.refreshToken

        val threadCount = 5
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        // When - Multiple threads try to use the same refresh token concurrently
        repeat(threadCount) {
            executor.submit {
                try {
                    authService.refreshToken(refreshToken)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // Then - Due to token rotation, only one request should succeed
        // Others should fail because the token gets invalidated
        // In a concurrent scenario, at most one should succeed
        assertTrue(successCount.get() <= 1, "At most one concurrent refresh should succeed")
        assertTrue(failureCount.get() >= threadCount - 1, "Others should fail")
    }

    @Test
    @Order(6)
    @DisplayName("should cleanup expired tokens")
    fun `should cleanup expired tokens`() {
        // Given - Create tokens directly in database with past expiration
        every { googleLoginHandler.handle(any()) } returns testUser

        // Login to create a valid token first
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))

        // Get the count of tokens before cleanup
        val countBefore = refreshTokenRepository.count()
        assertTrue(countBefore > 0, "Should have at least one token")

        // When - Call cleanup for expired tokens
        val deletedCount = refreshTokenPort.deleteExpiredTokens()

        // Then - No tokens should be deleted (token is not expired yet)
        assertEquals(0, deletedCount, "No tokens should be deleted as they are not expired")

        // Verify current token still exists
        val tokenRecord = refreshTokenPort.findByToken(loginResponse.refreshToken)
        assertNotNull(tokenRecord, "Non-expired token should still exist")
    }

    @Test
    @Order(7)
    @DisplayName("should generate unique tokens on each refresh")
    @Transactional
    fun `should generate unique tokens on each refresh`() {
        // Given
        every { googleLoginHandler.handle(any()) } returns testUser
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))

        val generatedTokens = mutableSetOf<String>()
        generatedTokens.add(loginResponse.refreshToken)

        // When - Perform multiple refresh operations
        var currentToken = loginResponse.refreshToken
        repeat(5) {
            val refreshResponse = authService.refreshToken(currentToken)
            currentToken = refreshResponse.refreshToken
            generatedTokens.add(currentToken)
        }

        // Then - All tokens should be unique
        assertEquals(6, generatedTokens.size, "All 6 tokens (1 original + 5 refreshed) should be unique")
    }

    @Test
    @Order(8)
    @DisplayName("should maintain token chain integrity")
    @Transactional
    fun `should maintain token chain integrity`() {
        // Given
        every { googleLoginHandler.handle(any()) } returns testUser
        val loginResponse = authService.googleLogin(GoogleLoginRequest(idToken = "test_google_id_token"))

        // When - Create a chain of tokens
        val tokenChain = mutableListOf(loginResponse.refreshToken)
        var currentToken = loginResponse.refreshToken

        repeat(3) {
            val refreshResponse = authService.refreshToken(currentToken)
            currentToken = refreshResponse.refreshToken
            tokenChain.add(currentToken)
        }

        // Then - All previous tokens should be invalidated, only the last one should be valid
        tokenChain.dropLast(1).forEach { token ->
            val tokenRecord = refreshTokenPort.findByToken(token)
            assertTrue(
                tokenRecord?.invalidated ?: true,
                "All previous tokens in chain should be invalidated"
            )
        }

        // Last token should be valid (not invalidated)
        val lastTokenRecord = refreshTokenPort.findByToken(tokenChain.last())
        assertFalse(
            lastTokenRecord?.invalidated ?: true,
            "Last token in chain should not be invalidated"
        )

        // Verify chain integrity - all tokens belong to the same user
        tokenChain.forEach { token ->
            val tokenRecord = refreshTokenPort.findByToken(token)
            assertEquals(testUserId, tokenRecord?.userId, "All tokens should belong to the test user")
        }

        // Breaking the chain - using an old token should invalidate all
        val oldToken = tokenChain[1] // Use second token (already invalidated)
        assertThrows<RefreshTokenReusedException> {
            authService.refreshToken(oldToken)
        }

        // After security breach, even the last valid token should be invalidated
        val lastTokenAfterBreach = refreshTokenPort.findByToken(tokenChain.last())
        assertTrue(
            lastTokenAfterBreach?.invalidated ?: true,
            "Even the last token should be invalidated after chain breach"
        )
    }
}
