package com.fanpulse.infrastructure.security

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import jakarta.servlet.http.HttpServletResponse

private const val SC_TOO_MANY_REQUESTS = 429

/**
 * RateLimitFilter TDD Tests
 *
 * Phase 2: JWT Security Hardening
 * Rate Limiting은 인증 엔드포인트에 대한 무차별 대입 공격을 방지합니다.
 *
 * RED Phase: 이 테스트들은 RateLimitFilter 구현 전 먼저 실패해야 합니다.
 */
@DisplayName("RateLimitFilter")
class RateLimitFilterTest {

    private lateinit var rateLimitFilter: RateLimitFilter
    private lateinit var filterChain: MockFilterChain

    @BeforeEach
    fun setUp() {
        rateLimitFilter = RateLimitFilter()
        filterChain = MockFilterChain()
    }

    @Nested
    @DisplayName("Login Endpoint Rate Limiting")
    inner class LoginRateLimiting {

        @Test
        @DisplayName("첫 번째 로그인 요청은 허용되어야 한다")
        fun `should allow first login request`() {
            // Given
            val request = createLoginRequest("192.168.1.1")
            val response = MockHttpServletResponse()

            // When
            rateLimitFilter.doFilter(request, response, filterChain)

            // Then
            assertNotEquals(SC_TOO_MANY_REQUESTS, response.status)
        }

        @Test
        @DisplayName("제한 횟수 이하의 로그인 요청은 모두 허용되어야 한다")
        fun `should allow requests within rate limit`() {
            // Given
            val clientIp = "192.168.1.2"
            val responses = mutableListOf<MockHttpServletResponse>()

            // When - 5번 요청 (제한 내)
            repeat(5) {
                val request = createLoginRequest(clientIp)
                val response = MockHttpServletResponse()
                rateLimitFilter.doFilter(request, response, MockFilterChain())
                responses.add(response)
            }

            // Then - 모두 허용되어야 함
            responses.forEach { response ->
                assertNotEquals(
                    SC_TOO_MANY_REQUESTS,
                    response.status,
                    "Request should be allowed within rate limit"
                )
            }
        }

        @Test
        @DisplayName("제한 횟수 초과 시 429 Too Many Requests를 반환해야 한다")
        fun `should return 429 when rate limit exceeded`() {
            // Given
            val clientIp = "192.168.1.3"

            // When - 제한 횟수 초과 (6번째 요청)
            repeat(5) {
                rateLimitFilter.doFilter(
                    createLoginRequest(clientIp),
                    MockHttpServletResponse(),
                    MockFilterChain()
                )
            }

            // 6번째 요청
            val request = createLoginRequest(clientIp)
            val response = MockHttpServletResponse()
            rateLimitFilter.doFilter(request, response, MockFilterChain())

            // Then
            assertEquals(
                SC_TOO_MANY_REQUESTS,
                response.status,
                "Should return 429 when rate limit exceeded"
            )
        }

        @Test
        @DisplayName("429 응답에 Retry-After 헤더가 포함되어야 한다")
        fun `should include Retry-After header when rate limited`() {
            // Given
            val clientIp = "192.168.1.4"

            // When - 제한 초과
            repeat(6) {
                rateLimitFilter.doFilter(
                    createLoginRequest(clientIp),
                    MockHttpServletResponse(),
                    MockFilterChain()
                )
            }

            val request = createLoginRequest(clientIp)
            val response = MockHttpServletResponse()
            rateLimitFilter.doFilter(request, response, MockFilterChain())

            // Then
            assertNotNull(
                response.getHeader("Retry-After"),
                "Should include Retry-After header"
            )
        }

        @Test
        @DisplayName("다른 IP의 요청은 별도로 제한되어야 한다")
        fun `should rate limit per IP address`() {
            // Given
            val clientIp1 = "192.168.1.5"
            val clientIp2 = "192.168.1.6"

            // When - IP1의 제한 소진
            repeat(6) {
                rateLimitFilter.doFilter(
                    createLoginRequest(clientIp1),
                    MockHttpServletResponse(),
                    MockFilterChain()
                )
            }

            // IP2의 첫 번째 요청
            val request = createLoginRequest(clientIp2)
            val response = MockHttpServletResponse()
            rateLimitFilter.doFilter(request, response, MockFilterChain())

            // Then - IP2는 여전히 허용되어야 함
            assertNotEquals(
                SC_TOO_MANY_REQUESTS,
                response.status,
                "Different IP should have separate rate limit"
            )
        }
    }

    @Nested
    @DisplayName("Non-Login Endpoint")
    inner class NonLoginEndpoint {

        @Test
        @DisplayName("로그인 외 엔드포인트는 Rate Limiting이 적용되지 않아야 한다")
        fun `should not rate limit non-login endpoints`() {
            // Given
            val clientIp = "192.168.1.7"

            // When - 많은 요청을 보냄
            repeat(20) {
                val request = createNonLoginRequest(clientIp)
                val response = MockHttpServletResponse()
                rateLimitFilter.doFilter(request, response, MockFilterChain())

                // Then - 모두 허용
                assertNotEquals(
                    SC_TOO_MANY_REQUESTS,
                    response.status,
                    "Non-login endpoint should not be rate limited"
                )
            }
        }
    }

    @Nested
    @DisplayName("Register Endpoint Rate Limiting")
    inner class RegisterRateLimiting {

        @Test
        @DisplayName("회원가입 엔드포인트도 Rate Limiting이 적용되어야 한다")
        fun `should rate limit register endpoint`() {
            // Given
            val clientIp = "192.168.1.8"

            // When - 제한 초과
            repeat(6) {
                rateLimitFilter.doFilter(
                    createRegisterRequest(clientIp),
                    MockHttpServletResponse(),
                    MockFilterChain()
                )
            }

            val request = createRegisterRequest(clientIp)
            val response = MockHttpServletResponse()
            rateLimitFilter.doFilter(request, response, MockFilterChain())

            // Then
            assertEquals(
                SC_TOO_MANY_REQUESTS,
                response.status,
                "Register endpoint should be rate limited"
            )
        }
    }

    @Nested
    @DisplayName("X-Forwarded-For 헤더 처리")
    inner class ForwardedForHeader {

        @Test
        @DisplayName("X-Forwarded-For 헤더가 있으면 해당 IP로 제한해야 한다")
        fun `should use X-Forwarded-For header for rate limiting`() {
            // Given
            val proxyIp = "10.0.0.1"
            val realClientIp = "192.168.1.9"

            // When - 프록시를 통한 요청
            repeat(6) {
                val request = createLoginRequest(proxyIp).apply {
                    addHeader("X-Forwarded-For", realClientIp)
                }
                rateLimitFilter.doFilter(request, MockHttpServletResponse(), MockFilterChain())
            }

            // 같은 실제 클라이언트 IP로 추가 요청
            val request = createLoginRequest(proxyIp).apply {
                addHeader("X-Forwarded-For", realClientIp)
            }
            val response = MockHttpServletResponse()
            rateLimitFilter.doFilter(request, response, MockFilterChain())

            // Then - 실제 클라이언트 IP 기준으로 제한
            assertEquals(
                SC_TOO_MANY_REQUESTS,
                response.status
            )
        }
    }

    @Nested
    @DisplayName("응답 본문")
    inner class ResponseBody {

        @Test
        @DisplayName("429 응답에 RFC 7807 형식의 에러 본문이 포함되어야 한다")
        fun `should return RFC 7807 problem detail on rate limit`() {
            // Given
            val clientIp = "192.168.1.10"

            // When - 제한 초과
            repeat(6) {
                rateLimitFilter.doFilter(
                    createLoginRequest(clientIp),
                    MockHttpServletResponse(),
                    MockFilterChain()
                )
            }

            val request = createLoginRequest(clientIp)
            val response = MockHttpServletResponse()
            rateLimitFilter.doFilter(request, response, MockFilterChain())

            // Then
            assertEquals("application/problem+json", response.contentType)
            assertTrue(response.contentAsString.contains("rate-limit-exceeded"))
        }
    }

    // Helper methods
    private fun createLoginRequest(remoteAddr: String): MockHttpServletRequest {
        return MockHttpServletRequest("POST", "/api/v1/auth/login").apply {
            this.remoteAddr = remoteAddr
            contentType = "application/json"
        }
    }

    private fun createRegisterRequest(remoteAddr: String): MockHttpServletRequest {
        return MockHttpServletRequest("POST", "/api/v1/auth/register").apply {
            this.remoteAddr = remoteAddr
            contentType = "application/json"
        }
    }

    private fun createNonLoginRequest(remoteAddr: String): MockHttpServletRequest {
        return MockHttpServletRequest("GET", "/api/v1/users/me").apply {
            this.remoteAddr = remoteAddr
        }
    }
}
