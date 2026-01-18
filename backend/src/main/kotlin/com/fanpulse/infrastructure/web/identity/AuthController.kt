package com.fanpulse.infrastructure.web.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.application.service.identity.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@Tag(name = "Authentication", description = "Google OAuth 인증 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "Google 로그인", description = "Google ID Token으로 로그인")
    @PostMapping("/google")
    fun googleLogin(@Valid @RequestBody request: GoogleLoginRequest): ResponseEntity<TokenResponse> {
        logger.info { "Google login request received" }
        val response = authService.googleLogin(request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰 갱신")
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        logger.info { "Token refresh request received" }
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리")
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<Void> {
        val token = authorization.removePrefix("Bearer ").trim()
        logger.info { "Logout request received" }
        authService.logout(token)
        return ResponseEntity.noContent().build()
    }
}
