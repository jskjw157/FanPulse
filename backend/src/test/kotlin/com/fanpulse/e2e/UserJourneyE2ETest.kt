package com.fanpulse.e2e

import com.fanpulse.application.identity.*
import com.fanpulse.application.identity.command.GoogleLoginHandler
import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.infrastructure.persistence.identity.RefreshTokenJpaRepositoryInterface
import com.fanpulse.application.dto.identity.RefreshTokenRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

/**
 * E2E User Journey Tests
 *
 * Tests complete user journeys through the API:
 * 1. New User Journey: Login -> Profile -> Live List -> Detail -> Logout
 * 2. Token Refresh Journey: Login -> Refresh -> API Call -> Logout
 * 3. Security Breach Detection: Login -> Refresh -> Reuse Old Token -> Block
 *
 * Uses @SpringBootTest for full application context with H2 in-memory database.
 * GoogleLoginHandler is mocked to avoid actual Google OAuth calls.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User Journey E2E Tests")
class UserJourneyE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userPort: UserPort

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenJpaRepositoryInterface

    @MockkBean
    private lateinit var googleLoginHandler: GoogleLoginHandler

    private lateinit var testUser: User
    private lateinit var testUserId: UUID
    private val testEmail = "e2e-testuser-${System.currentTimeMillis()}@gmail.com"
    private val testUsername = "e2etestuser${System.currentTimeMillis()}"

    @BeforeEach
    fun setUp() {
        // Clean up refresh tokens
        refreshTokenRepository.deleteAll()

        // Create and save a test user
        testUserId = UUID.randomUUID()
        testUser = User.registerWithOAuth(
            email = Email.of(testEmail),
            username = Username.of(testUsername)
        )

        // Use reflection to set user ID for consistent testing
        val idField = User::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(testUser, testUserId)

        // Save the test user
        userPort.save(testUser)
    }

    @AfterEach
    fun tearDown() {
        refreshTokenRepository.deleteAll()
        try {
            userPort.deleteById(testUserId)
        } catch (e: Exception) {
            // Ignore if user doesn't exist
        }
    }

    /**
     * Helper to perform Google login and extract tokens.
     */
    private fun performGoogleLogin(idToken: String = "valid_google_id_token"): AuthResponse {
        every { googleLoginHandler.handle(any()) } returns testUser

        val request = GoogleLoginRequest(idToken = idToken)

        val result = mockMvc.post("/api/v1/auth/google") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        return objectMapper.readValue(result.response.contentAsString, AuthResponse::class.java)
    }

    /**
     * Helper to perform token refresh.
     */
    private fun performTokenRefresh(refreshToken: String): TokenResponse {
        val request = RefreshTokenRequest(refreshToken = refreshToken)

        val result = mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        return objectMapper.readValue(result.response.contentAsString, TokenResponse::class.java)
    }

    // =====================================================
    // Journey 1: New User Journey
    // =====================================================

    @Nested
    @DisplayName("Journey 1: New User Journey")
    inner class NewUserJourney {

        @Test
        @DisplayName("Complete journey: Login -> Profile -> Live List -> Detail -> Logout")
        fun `should complete new user journey successfully`() {
            // Step 1: Google Login
            val authResponse = performGoogleLogin()

            assertNotNull(authResponse.userId, "User ID should not be null")
            assertNotNull(authResponse.accessToken, "Access token should not be null")
            assertNotNull(authResponse.refreshToken, "Refresh token should not be null")
            assertEquals(testUserId, authResponse.userId, "User ID should match")

            val accessToken = authResponse.accessToken

            // Step 2: Get Profile (GET /api/v1/me)
            // Note: MeController uses @RequestAttribute("userId") which is set by JwtAuthenticationFilter
            mockMvc.get("/api/v1/me") {
                header("Authorization", "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
            }

            // Step 3: Get Streaming Events List (GET /api/v1/streaming-events)
            mockMvc.get("/api/v1/streaming-events") {
                param("limit", "20")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.items") { isArray() }
                jsonPath("$.data.hasMore") { exists() }
            }

            // Step 4: Get Streaming Event Detail (with a non-existent ID - should return 404)
            val nonExistentId = UUID.randomUUID()
            mockMvc.get("/api/v1/streaming-events/{id}", nonExistentId).andExpect {
                status { isNotFound() }
            }

            // Step 5: Logout is implicit via token invalidation
            // In this test, the user journey is complete
        }

        @Test
        @DisplayName("Login with invalid Google token should return 401")
        fun `should reject login with invalid Google token`() {
            every { googleLoginHandler.handle(any()) } throws InvalidGoogleTokenException()

            val request = GoogleLoginRequest(idToken = "invalid_token")

            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        @DisplayName("Login with unverified email should return 400")
        fun `should reject login with unverified Google email`() {
            every { googleLoginHandler.handle(any()) } throws OAuthEmailNotVerifiedException()

            val request = GoogleLoginRequest(idToken = "unverified_email_token")

            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }

    // =====================================================
    // Journey 2: Token Refresh Journey
    // =====================================================

    @Nested
    @DisplayName("Journey 2: Token Refresh Journey")
    inner class TokenRefreshJourney {

        @Test
        @DisplayName("Complete journey: Login -> Refresh Token -> API Call -> Success")
        fun `should complete token refresh journey successfully`() {
            // Step 1: Login and get tokens
            val authResponse = performGoogleLogin()
            val originalAccessToken = authResponse.accessToken
            val originalRefreshToken = authResponse.refreshToken

            assertNotNull(originalRefreshToken, "Original refresh token should not be null")

            // Step 2: Refresh tokens
            val tokenResponse = performTokenRefresh(originalRefreshToken)

            assertNotNull(tokenResponse.accessToken, "New access token should not be null")
            assertNotNull(tokenResponse.refreshToken, "New refresh token should not be null")
            assertNotEquals(originalAccessToken, tokenResponse.accessToken, "New access token should differ")
            assertNotEquals(originalRefreshToken, tokenResponse.refreshToken, "New refresh token should differ (rotation)")

            val newAccessToken = tokenResponse.accessToken

            // Step 3: Use new access token for API call
            mockMvc.get("/api/v1/streaming-events") {
                header("Authorization", "Bearer $newAccessToken")
                param("limit", "10")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }
        }

        @Test
        @DisplayName("Refresh with invalid token should return 401")
        fun `should reject refresh with invalid token`() {
            val invalidRefreshToken = "invalid_refresh_token"

            val request = RefreshTokenRequest(refreshToken = invalidRefreshToken)

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        @DisplayName("Multiple refreshes should generate different tokens each time")
        fun `should generate different tokens on each refresh`() {
            // Login
            val authResponse = performGoogleLogin()
            var currentRefreshToken = authResponse.refreshToken

            // First refresh
            val firstRefresh = performTokenRefresh(currentRefreshToken)
            val firstNewAccessToken = firstRefresh.accessToken
            val firstNewRefreshToken = firstRefresh.refreshToken

            assertNotEquals(currentRefreshToken, firstNewRefreshToken)
            currentRefreshToken = firstNewRefreshToken

            // Second refresh
            val secondRefresh = performTokenRefresh(currentRefreshToken)
            val secondNewAccessToken = secondRefresh.accessToken
            val secondNewRefreshToken = secondRefresh.refreshToken

            assertNotEquals(firstNewAccessToken, secondNewAccessToken)
            assertNotEquals(firstNewRefreshToken, secondNewRefreshToken)
        }
    }

    // =====================================================
    // Journey 3: Security Breach Detection
    // =====================================================

    @Nested
    @DisplayName("Journey 3: Security Breach Detection")
    inner class SecurityBreachDetection {

        @Test
        @DisplayName("Reusing invalidated refresh token should return 401 and invalidate all tokens")
        fun `should detect and block refresh token reuse`() {
            // Step 1: Login and get initial tokens
            val authResponse = performGoogleLogin()
            val oldRefreshToken = authResponse.refreshToken

            assertNotNull(oldRefreshToken, "Initial refresh token should not be null")

            // Step 2: Use refresh token to get new tokens (this invalidates the old one)
            val newTokenResponse = performTokenRefresh(oldRefreshToken)
            val newRefreshToken = newTokenResponse.refreshToken

            assertNotEquals(oldRefreshToken, newRefreshToken, "New refresh token should be different")

            // Step 3: Attempt to reuse the OLD (now invalidated) refresh token
            // This simulates a security breach where attacker tries to use stolen token
            val request = RefreshTokenRequest(refreshToken = oldRefreshToken)

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }  // Should be rejected
            }

            // Step 4: Verify that even the new token is now invalidated
            // (Token Rotation Security: when reuse is detected, ALL tokens are invalidated)
            val newTokenRequest = RefreshTokenRequest(refreshToken = newRefreshToken)

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(newTokenRequest)
            }.andExpect {
                status { isUnauthorized() }  // Should also be rejected due to invalidation
            }
        }

        @Test
        @DisplayName("Should track refresh token chain and detect theft")
        fun `should invalidate all tokens on security breach detection`() {
            // This test verifies the complete token family invalidation

            // Step 1: Login
            val authResponse = performGoogleLogin()
            val token1 = authResponse.refreshToken

            // Step 2: Legitimate refresh -> token2
            val response2 = performTokenRefresh(token1)
            val token2 = response2.refreshToken

            // Step 3: Legitimate refresh -> token3
            val response3 = performTokenRefresh(token2)
            val token3 = response3.refreshToken

            // Step 4: Attacker tries to use stolen token1 (already used/invalidated)
            val attackRequest = RefreshTokenRequest(refreshToken = token1)

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(attackRequest)
            }.andExpect {
                status { isUnauthorized() }
            }

            // Step 5: Even token3 (the latest legitimate token) should now be invalid
            // because the entire token family was invalidated
            val legitimateRequest = RefreshTokenRequest(refreshToken = token3)

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(legitimateRequest)
            }.andExpect {
                status { isUnauthorized() }
            }

            // Step 6: User must re-login to get new tokens
            val reAuthResponse = performGoogleLogin()
            assertNotNull(reAuthResponse.accessToken, "User should be able to re-login")
        }

        @Test
        @DisplayName("Empty refresh token should return 401")
        fun `should reject empty refresh token`() {
            val request = RefreshTokenRequest(refreshToken = "")

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        @DisplayName("Malformed JWT token should return 401")
        fun `should reject malformed JWT token`() {
            val request = RefreshTokenRequest(refreshToken = "not.a.valid.jwt.token")

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }
    }
}
