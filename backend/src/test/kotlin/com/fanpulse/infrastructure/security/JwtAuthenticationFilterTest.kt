package com.fanpulse.infrastructure.security

import io.mockk.*
import io.mockk.junit5.MockKExtension
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

/**
 * JwtAuthenticationFilter TDD Tests
 */
@ExtendWith(MockKExtension::class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    private lateinit var filter: JwtAuthenticationFilter
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mockk()
        filter = JwtAuthenticationFilter(jwtTokenProvider)

        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
        filterChain = mockk(relaxed = true)

        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Nested
    @DisplayName("Authorization 헤더 처리")
    inner class AuthorizationHeader {

        @Test
        @DisplayName("유효한 Bearer 토큰이 있으면 SecurityContext에 인증 정보가 설정되어야 한다")
        fun `should set authentication when valid bearer token provided`() {
            // Given
            val userId = UUID.randomUUID()
            val token = "valid_token"

            request.addHeader("Authorization", "Bearer $token")
            every { jwtTokenProvider.validateToken(token) } returns true
            every { jwtTokenProvider.getTokenType(token) } returns "access"
            every { jwtTokenProvider.getUserIdFromToken(token) } returns userId
            every { filterChain.doFilter(request, response) } just runs

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNotNull(authentication, "Authentication should be set")
            assertEquals(userId.toString(), authentication?.principal)

            verify { filterChain.doFilter(request, response) }
        }

        @Test
        @DisplayName("Authorization 헤더가 없으면 인증 없이 진행해야 한다")
        fun `should proceed without authentication when no header`() {
            // Given - no header added
            every { filterChain.doFilter(request, response) } just runs

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication, "Authentication should not be set")

            verify { filterChain.doFilter(request, response) }
        }

        @Test
        @DisplayName("Bearer 접두사가 없으면 인증 없이 진행해야 한다")
        fun `should proceed without authentication when no bearer prefix`() {
            // Given
            request.addHeader("Authorization", "Basic token123")
            every { filterChain.doFilter(request, response) } just runs

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication, "Authentication should not be set")

            verify { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    inner class TokenValidation {

        @Test
        @DisplayName("유효하지 않은 토큰이면 인증 없이 진행해야 한다")
        fun `should proceed without authentication when token is invalid`() {
            // Given
            val token = "invalid_token"

            request.addHeader("Authorization", "Bearer $token")
            every { jwtTokenProvider.validateToken(token) } returns false
            every { filterChain.doFilter(request, response) } just runs

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication, "Authentication should not be set for invalid token")

            verify { filterChain.doFilter(request, response) }
        }

        @Test
        @DisplayName("Refresh 토큰이면 인증 없이 진행해야 한다")
        fun `should proceed without authentication when refresh token provided`() {
            // Given
            val token = "refresh_token"

            request.addHeader("Authorization", "Bearer $token")
            every { jwtTokenProvider.validateToken(token) } returns true
            every { jwtTokenProvider.getTokenType(token) } returns "refresh"
            every { filterChain.doFilter(request, response) } just runs

            // When
            filter.doFilter(request, response, filterChain)

            // Then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication, "Authentication should not be set for refresh token")

            verify { filterChain.doFilter(request, response) }
        }
    }
}
