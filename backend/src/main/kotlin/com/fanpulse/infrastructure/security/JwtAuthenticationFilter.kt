package com.fanpulse.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

/**
 * JWT Authentication Filter.
 *
 * Extracts JWT from Authorization header and sets authentication in SecurityContext.
 * Public 경로는 필터를 건너뛰어 불필요한 토큰 검증을 방지합니다.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "

        /** Public 경로 - 인증 없이 접근 가능 */
        private val PUBLIC_PATHS = listOf(
            "/api/v1/auth/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**"
        )
    }

    /**
     * Public 경로는 필터 적용하지 않음
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        return PUBLIC_PATHS.any { pattern -> pathMatcher.match(pattern, path) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractToken(request)

            if (token != null && authenticateToken(token)) {
                val userId = jwtTokenProvider.getUserIdFromToken(token)
                setAuthentication(userId.toString())
                // Set userId as request attribute for @RequestAttribute("userId") in controllers
                request.setAttribute("userId", userId)
                logger.debug { "Authenticated user: $userId" }
            }
        } catch (e: Exception) {
            logger.debug { "Could not authenticate request: ${e.message}" }
            // Clear any partial authentication
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Extracts JWT token from Authorization header.
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader(AUTHORIZATION_HEADER)

        if (header.isNullOrBlank() || !header.startsWith(BEARER_PREFIX)) {
            return null
        }

        return header.substring(BEARER_PREFIX.length)
    }

    /**
     * Validates the token and checks if it's an access token.
     *
     * SECURITY: Refresh Token은 Access Token보다 유효기간이 길기 때문에,
     * Authorization 헤더에 Refresh Token을 사용하는 것을 명시적으로 거부합니다.
     */
    private fun authenticateToken(token: String): Boolean {
        if (!jwtTokenProvider.validateToken(token)) {
            logger.debug { "Invalid JWT token" }
            return false
        }

        // SECURITY: Refresh Token은 인증에 사용할 수 없음
        if (!jwtTokenProvider.isAccessToken(token)) {
            if (jwtTokenProvider.isRefreshToken(token)) {
                logger.warn { "Refresh token을 Authorization 헤더에 사용 시도 감지 - 거부됨" }
            }
            return false
        }

        return true
    }

    /**
     * Sets authentication in SecurityContext.
     */
    private fun setAuthentication(userId: String) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
    }
}
