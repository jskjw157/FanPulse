package com.fanpulse.infrastructure.security

import com.fanpulse.domain.identity.port.JwtTokenPort
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증 필터
 *
 * 모든 요청에서 Authorization 헤더의 JWT 토큰을 검증하고,
 * 유효한 경우 SecurityContext에 인증 정보를 설정합니다.
 *
 * Clean Architecture:
 * - Infrastructure 계층에 위치
 * - Domain Port (JwtTokenPort)를 통해 토큰 검증
 * - Spring Security Filter Chain에 통합
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenPort: JwtTokenPort
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)
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

            if (token != null && token.isNotBlank()) {
                authenticateWithToken(token)
            }
        } catch (e: Exception) {
            log.debug("JWT 인증 실패: ${e.message}")
            // 예외 발생 시 SecurityContext를 설정하지 않음
            // 다음 필터로 전달하여 인증이 필요한 엔드포인트는 401 반환
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출합니다.
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)

        return if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length).takeIf { it.isNotBlank() }
        } else {
            null
        }
    }

    /**
     * 토큰을 검증하고 SecurityContext에 인증 정보를 설정합니다.
     */
    private fun authenticateWithToken(token: String) {
        if (!jwtTokenPort.validateToken(token)) {
            log.debug("유효하지 않은 JWT 토큰")
            return
        }

        val userId = jwtTokenPort.getUserIdFromToken(token)
        if (userId == null) {
            log.debug("JWT 토큰에서 userId 추출 실패")
            return
        }

        // 인증 객체 생성 및 SecurityContext에 설정
        val authentication = UsernamePasswordAuthenticationToken(
            userId.toString(),
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        SecurityContextHolder.getContext().authentication = authentication
        log.debug("JWT 인증 성공: userId=$userId")
    }
}
