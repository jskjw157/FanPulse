package com.fanpulse.infrastructure.security

import com.fanpulse.domain.identity.port.JwtTokenPort
import io.mockk.*
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

/**
 * JwtAuthenticationFilter 단위 테스트
 *
 * TDD RED Phase: 필터가 구현되기 전 테스트 작성
 */
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    private lateinit var jwtTokenPort: JwtTokenPort
    private lateinit var filter: JwtAuthenticationFilter
    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        jwtTokenPort = mockk()
        filter = JwtAuthenticationFilter(jwtTokenPort)
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
    @DisplayName("유효한 JWT 토큰")
    inner class ValidToken {

        @Test
        @DisplayName("유효한 Access Token → 인증 성공, SecurityContext에 Authentication 설정")
        fun shouldSetAuthenticationWhenValidToken() {
            // given
            val userId = UUID.randomUUID()
            val validToken = "valid.jwt.token"
            request.addHeader("Authorization", "Bearer $validToken")

            every { jwtTokenPort.validateToken(validToken) } returns true
            every { jwtTokenPort.isAccessToken(validToken) } returns true
            every { jwtTokenPort.getUserIdFromToken(validToken) } returns userId

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNotNull(authentication)
            assertEquals(userId.toString(), authentication.principal)
            assertTrue(authentication.isAuthenticated)
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        @DisplayName("Refresh Token을 Authorization 헤더에 사용 시도 → SecurityContext에 Authentication 없음")
        fun shouldRejectRefreshTokenInAuthorizationHeader() {
            // given
            val refreshToken = "valid.refresh.token"
            request.addHeader("Authorization", "Bearer $refreshToken")

            every { jwtTokenPort.validateToken(refreshToken) } returns true
            every { jwtTokenPort.isAccessToken(refreshToken) } returns false  // Refresh Token

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)  // Refresh Token은 인증에 사용 불가
            verify { filterChain.doFilter(request, response) }
            verify(exactly = 0) { jwtTokenPort.getUserIdFromToken(any()) }  // userId 추출 시도 안함
        }
    }

    @Nested
    @DisplayName("무효한 JWT 토큰")
    inner class InvalidToken {

        @Test
        @DisplayName("만료된 JWT → SecurityContext에 Authentication 없음")
        fun shouldNotSetAuthenticationWhenExpiredToken() {
            // given
            val expiredToken = "expired.jwt.token"
            request.addHeader("Authorization", "Bearer $expiredToken")

            every { jwtTokenPort.validateToken(expiredToken) } returns false

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        @DisplayName("잘못된 형식의 JWT → SecurityContext에 Authentication 없음")
        fun shouldNotSetAuthenticationWhenMalformedToken() {
            // given
            val malformedToken = "not-a-valid-jwt"
            request.addHeader("Authorization", "Bearer $malformedToken")

            every { jwtTokenPort.validateToken(malformedToken) } returns false

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        @DisplayName("토큰 검증 중 예외 발생 → SecurityContext에 Authentication 없음, 다음 필터 호출")
        fun shouldNotSetAuthenticationWhenExceptionOccurs() {
            // given
            val badToken = "bad.jwt.token"
            request.addHeader("Authorization", "Bearer $badToken")

            every { jwtTokenPort.validateToken(badToken) } throws RuntimeException("Token parsing error")

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
        }
    }

    @Nested
    @DisplayName("Authorization 헤더 없음")
    inner class NoAuthorizationHeader {

        @Test
        @DisplayName("Authorization 헤더 없음 → 다음 필터로 전달")
        fun shouldPassToNextFilterWhenNoAuthorizationHeader() {
            // given - no Authorization header

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
            verify(exactly = 0) { jwtTokenPort.validateToken(any()) }
        }

        @Test
        @DisplayName("Authorization 헤더가 'Bearer '로 시작하지 않음 → 다음 필터로 전달")
        fun shouldPassToNextFilterWhenNotBearerToken() {
            // given
            request.addHeader("Authorization", "Basic someCredentials")

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
            verify(exactly = 0) { jwtTokenPort.validateToken(any()) }
        }

        @Test
        @DisplayName("Authorization 헤더가 빈 문자열 → 다음 필터로 전달")
        fun shouldPassToNextFilterWhenEmptyAuthorizationHeader() {
            // given
            request.addHeader("Authorization", "")

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
            verify(exactly = 0) { jwtTokenPort.validateToken(any()) }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCases {

        @Test
        @DisplayName("토큰에서 userId 추출 실패 → SecurityContext에 Authentication 없음")
        fun shouldNotSetAuthenticationWhenUserIdExtractionFails() {
            // given
            val validToken = "valid.jwt.token"
            request.addHeader("Authorization", "Bearer $validToken")

            every { jwtTokenPort.validateToken(validToken) } returns true
            every { jwtTokenPort.isAccessToken(validToken) } returns true
            every { jwtTokenPort.getUserIdFromToken(validToken) } returns null

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
        }

        @Test
        @DisplayName("Bearer 뒤에 토큰이 없음 → 다음 필터로 전달")
        fun shouldPassToNextFilterWhenBearerWithoutToken() {
            // given
            request.addHeader("Authorization", "Bearer ")

            // when
            filter.doFilter(request, response, filterChain)

            // then
            val authentication = SecurityContextHolder.getContext().authentication
            assertNull(authentication)
            verify { filterChain.doFilter(request, response) }
        }
    }
}
