package com.fanpulse.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Rate Limiting Filter for Authentication Endpoints
 *
 * ## Rate Limiting 정책
 * - 대상 엔드포인트: /api/v1/auth/login, /api/v1/auth/register
 * - 제한: 클라이언트 IP당 분당 5회 요청
 * - 초과 시 429 Too Many Requests 반환
 *
 * ## 보안 목적
 * - 무차별 대입 공격 (Brute Force) 방지
 * - 계정 열거 공격 (Account Enumeration) 방지
 * - DDoS 공격 완화
 *
 * ## 테스트 환경
 * - `fanpulse.security.rate-limit.enabled=false`로 비활성화 가능
 */
@Component
@ConditionalOnProperty(
    name = ["fanpulse.security.rate-limit.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class RateLimitFilter : OncePerRequestFilter() {

    companion object {
        private const val REQUESTS_PER_MINUTE = 5L
        private const val REFILL_DURATION_MINUTES = 1L

        private val RATE_LIMITED_PATHS = setOf(
            "/api/v1/auth/login",
            "/api/v1/auth/register"
        )
    }

    private val buckets = ConcurrentHashMap<String, Bucket>()
    private val objectMapper = ObjectMapper()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestPath = request.requestURI

        // Rate limiting only for auth endpoints
        if (!shouldRateLimit(requestPath)) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = getClientIp(request)
        val bucket = buckets.computeIfAbsent(clientIp) { createNewBucket() }

        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            // Request allowed
            response.setHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            filterChain.doFilter(request, response)
        } else {
            // Rate limit exceeded
            val waitTimeSeconds = probe.nanosToWaitForRefill / 1_000_000_000
            logger.warn { "Rate limit exceeded for IP: $clientIp on path: $requestPath" }

            sendRateLimitResponse(response, waitTimeSeconds)
        }
    }

    private fun shouldRateLimit(path: String): Boolean {
        return RATE_LIMITED_PATHS.any { path.startsWith(it) }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        // Check X-Forwarded-For header for clients behind proxy/load balancer
        val forwardedFor = request.getHeader("X-Forwarded-For")
        return if (!forwardedFor.isNullOrBlank()) {
            // Take the first IP (original client)
            forwardedFor.split(",").first().trim()
        } else {
            request.remoteAddr
        }
    }

    private fun createNewBucket(): Bucket {
        val limit = Bandwidth.simple(
            REQUESTS_PER_MINUTE,
            Duration.ofMinutes(REFILL_DURATION_MINUTES)
        )
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }

    private fun sendRateLimitResponse(response: HttpServletResponse, waitTimeSeconds: Long) {
        response.status = 429  // Too Many Requests
        response.contentType = "application/problem+json"
        response.setHeader("Retry-After", waitTimeSeconds.toString())

        val problemDetail = mapOf(
            "type" to "https://api.fanpulse.com/errors/rate-limit-exceeded",
            "title" to "Too Many Requests",
            "status" to 429,
            "detail" to "Rate limit exceeded. Please try again after $waitTimeSeconds seconds.",
            "errorCode" to "RATE_LIMIT_EXCEEDED"
        )

        response.writer.write(objectMapper.writeValueAsString(problemDetail))
    }
}
