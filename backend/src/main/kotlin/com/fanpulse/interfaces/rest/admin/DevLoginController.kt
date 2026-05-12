package com.fanpulse.interfaces.rest.admin

import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * dev/QA 전용 토큰 발급 컨트롤러.
 *
 * Google ID Token 검증 없이 임의 사용자 ID/이메일로 access/refresh 토큰을 즉시 발급한다.
 * 안드로이드 개발자가 OAuth 설정 없이 보호된 API 를 호출하기 위한 백도어.
 *
 * 보안:
 * - `@ConditionalOnProperty(matchIfMissing = false)` → 기본값에서 빈 자체가 등록되지 않아 404 반환.
 * - production 환경에서는 환경 변수 `FANPULSE_DEV_LOGIN_ENABLED` 를 절대 true 로 설정하면 안 된다.
 * - SecurityConfig 의 permitAll 매칭과 별개로 코드 경로 자체가 차단되는 2중 방어.
 */
@RestController
@RequestMapping("/api/v1/admin/dev-login")
@ConditionalOnProperty(
    name = ["fanpulse.dev-login.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@Tag(name = "Admin - Dev Login", description = "dev/QA 전용 토큰 발급 (production 비활성화)")
class DevLoginController(
    private val userPort: UserPort,
    private val tokenPort: TokenPort,
    private val refreshTokenPort: RefreshTokenPort,
    @Value("\${app.cookie.secure:false}") private val cookieSecure: Boolean,
    @Value("\${app.cookie.domain:}") private val cookieDomain: String,
    @Value("\${app.cookie.max-age:604800}") private val cookieMaxAge: Int
) {
    companion object {
        const val ACCESS_TOKEN_COOKIE = "fanpulse_access_token"
        const val REFRESH_TOKEN_COOKIE = "fanpulse_refresh_token"
        const val DEFAULT_TEST_EMAIL = "devlogin@fanpulse.local"
        const val DEFAULT_TEST_USERNAME = "dev_tester"
    }

    @PostMapping
    @Operation(
        summary = "Dev/QA 전용 토큰 발급",
        description = "Google ID Token 없이 즉시 access/refresh 토큰을 발급한다. " +
            "production 환경에서는 컨트롤러 빈이 등록되지 않아 404 를 반환한다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "토큰 발급 성공"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음 (정책에 따라)"),
        ApiResponse(responseCode = "400", description = "잘못된 요청")
    )
    @Transactional
    fun devLogin(
        @RequestBody(required = false) request: DevLoginRequest?,
        response: HttpServletResponse
    ): ResponseEntity<DevLoginResponse> {
        val req = request ?: DevLoginRequest()
        logger.warn { "🔓 DEV LOGIN 호출됨 — userId=${req.userId}, email=${req.email}" }

        val user = resolveUserForDevLogin(req.userId, req.email)

        val accessToken = tokenPort.generateAccessToken(user.id)
        val refreshToken = tokenPort.generateRefreshToken(user.id)
        val refreshExpiration = tokenPort.getRefreshTokenExpirationSeconds()
        val expiresAt = Instant.now().plusSeconds(refreshExpiration)
        refreshTokenPort.save(user.id, refreshToken, expiresAt)

        setAuthCookies(response, accessToken, refreshToken)

        logger.info { "🔓 DEV LOGIN 토큰 발급 완료 — userId=${user.id}, email=${user.email}" }

        return ResponseEntity.ok(
            DevLoginResponse(
                userId = user.id,
                email = user.email,
                username = user.username,
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = tokenPort.getAccessTokenExpirationSeconds(),
                refreshExpiresIn = refreshExpiration
            )
        )
    }

    /**
     * 사용자 조회/생성 정책을 결정하는 함수.
     *
     * dev-login 의 사용자 해석 정책은 dev/QA 환경의 운영 방식에 영향을 주는 비즈니스 결정이다:
     * - userId 가 주어지면 그것을 우선 사용
     * - email 이 주어지면 email 로 조회
     * - 둘 다 비어있으면 기본 테스트 사용자 사용
     *
     * 사용자가 존재하지 않을 때:
     *   (a) 자동 생성  — 매번 새 사용자 생성, 빠른 테스트
     *   (b) 404 반환   — 명시적 시드 데이터 강제, 안전
     *   (c) 기본 사용자로 폴백 — DEFAULT_TEST_EMAIL/USERNAME 으로 단일 공유 계정
     *
     * 가용한 도구:
     *   - userPort.findById(UUID), findByEmail(String), existsByEmail(String)
     *   - userPort.save(User) — User.registerWithOAuth(Email, Username) 또는 직접 생성
     *   - Email("..."), Username("...") 값 객체 생성자 사용 가능
     */
    private fun resolveUserForDevLogin(userId: UUID?, email: String?): User {
        if (userId != null) {
            return userPort.findById(userId)
                ?: throw NoSuchElementException("userId=$userId 사용자 없음")
        }
        if (!email.isNullOrBlank()) {
            userPort.findByEmail(email)?.let { return it }
            val newUser = User.registerWithOAuth(Email(email), Username(sanitizeUsername(email)))
            return userPort.save(newUser)
        }
        userPort.findByEmail(DEFAULT_TEST_EMAIL)?.let { return it }
        val defaultUser = User.registerWithOAuth(Email(DEFAULT_TEST_EMAIL), Username(DEFAULT_TEST_USERNAME))
        return userPort.save(defaultUser)
    }

    /**
     * email 의 local-part 를 Username 정규식([a-zA-Z0-9_-]{2,50})에 맞게 정규화한다.
     * Username 도메인 규칙: 길이 2-50, 영숫자/언더스코어/하이픈만 허용.
     */
    private fun sanitizeUsername(email: String): String {
        val cleaned = email.substringBefore('@')
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
            .take(50)
        return when {
            cleaned.length < 2 -> "u_${UUID.randomUUID().toString().take(8)}"
            else -> cleaned
        }
    }

    private fun setAuthCookies(
        response: HttpServletResponse,
        accessToken: String,
        refreshToken: String
    ) {
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, accessToken, cookieMaxAge))
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, refreshToken, cookieMaxAge * 2))
    }

    private fun createCookie(name: String, value: String, maxAge: Int): Cookie {
        return Cookie(name, value).apply {
            this.isHttpOnly = true
            this.secure = cookieSecure
            this.path = "/"
            this.maxAge = maxAge
            if (cookieDomain.isNotBlank()) {
                this.domain = cookieDomain
            }
            setAttribute("SameSite", "Lax")
        }
    }
}

/**
 * dev-login 요청 모델. 모든 필드가 선택적이다.
 * - userId 제공 → 해당 UUID 의 사용자로 발급
 * - email 제공 → 해당 이메일로 조회 (정책에 따라 생성 가능)
 * - 둘 다 미제공 → 기본 테스트 사용자
 */
@Schema(description = "Dev/QA 전용 토큰 발급 요청 (모든 필드 선택적)")
data class DevLoginRequest(
    @Schema(description = "발급 대상 사용자 UUID (선택)", example = "550e8400-e29b-41d4-a716-446655440000")
    val userId: UUID? = null,

    @Schema(description = "발급 대상 사용자 이메일 (선택)", example = "fan@example.com")
    val email: String? = null
)

/**
 * dev-login 응답 모델. 모바일 클라이언트가 헤더 방식으로도 사용할 수 있도록 토큰을 본문에 포함한다.
 */
@Schema(description = "Dev/QA 전용 토큰 발급 응답")
data class DevLoginResponse(
    val userId: UUID,
    val email: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val refreshExpiresIn: Long
)
