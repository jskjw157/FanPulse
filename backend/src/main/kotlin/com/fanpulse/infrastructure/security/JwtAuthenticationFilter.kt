package com.fanpulse.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

/**
 * JWT Authentication Filter.
 *
 * Extracts JWT from Authorization header and sets authentication in SecurityContext.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
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
     */
    private fun authenticateToken(token: String): Boolean {
        if (!jwtTokenProvider.validateToken(token)) {
            return false
        }

        val tokenType = jwtTokenProvider.getTokenType(token)
        return tokenType == "access"
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
