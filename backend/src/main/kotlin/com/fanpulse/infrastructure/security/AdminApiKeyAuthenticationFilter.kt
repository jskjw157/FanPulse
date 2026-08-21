package com.fanpulse.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private val adminAuthLogger = KotlinLogging.logger {}

/**
 * `/api/v1/admin/…` 경로 전용 API Key 인증 필터.
 *
 * 일반 사용자 JWT와 관리자 자격 증명을 분리하기 위해 관리자 요청은
 * [HEADER_NAME] 헤더의 전용 키로만 인증한다. 비교 전 양쪽 값을 SHA-256으로
 * 해시해 고정 길이 바이트 배열을 만든 뒤 상수 시간 비교를 수행한다.
 *
 * 키가 설정되지 않은 환경에서도 요청을 허용하지 않는 fail-closed 정책을 사용한다.
 */
class AdminApiKeyAuthenticationFilter(
    configuredApiKey: String,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    companion object {
        const val HEADER_NAME = "X-FanPulse-Admin-Key"
        private const val ADMIN_PATH = "/api/v1/admin"
        private const val ADMIN_PRINCIPAL = "fanpulse-admin-api-key"
        private const val ADMIN_AUTHORITY = "ROLE_ADMIN"
    }

    private val expectedDigest: ByteArray? = configuredApiKey
        .takeIf { it.isNotBlank() }
        ?.let(::sha256)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (request.method.equals("OPTIONS", ignoreCase = true)) {
            return true
        }

        val path = request.servletPath
        return path != ADMIN_PATH && !path.startsWith("$ADMIN_PATH/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val providedApiKey = request.getHeader(HEADER_NAME)
        if (!matchesConfiguredKey(providedApiKey)) {
            SecurityContextHolder.clearContext()
            sendUnauthorized(response)
            return
        }

        val authentication = UsernamePasswordAuthenticationToken(
            ADMIN_PRINCIPAL,
            null,
            listOf(SimpleGrantedAuthority(ADMIN_AUTHORITY)),
        )
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }

    private fun matchesConfiguredKey(providedApiKey: String?): Boolean {
        val configuredDigest = expectedDigest
        if (configuredDigest == null) {
            adminAuthLogger.error {
                "Admin API key is not configured; admin request was rejected"
            }
            return false
        }

        if (providedApiKey.isNullOrBlank()) {
            return false
        }

        return MessageDigest.isEqual(configuredDigest, sha256(providedApiKey))
    }

    private fun sendUnauthorized(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/problem+json"
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.setHeader("Cache-Control", "no-store")
        response.setHeader(
            "WWW-Authenticate",
            "ApiKey realm=\"FanPulse Admin\", header=\"$HEADER_NAME\"",
        )

        objectMapper.writeValue(
            response.writer,
            mapOf(
                "type" to "https://api.fanpulse.app/errors/admin-authentication-required",
                "title" to "Unauthorized",
                "status" to HttpServletResponse.SC_UNAUTHORIZED,
                "detail" to "A valid administrator API key is required.",
                "errorCode" to "ADMIN_AUTHENTICATION_REQUIRED",
            ),
        )
    }

    private fun sha256(value: String): ByteArray = MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
}
