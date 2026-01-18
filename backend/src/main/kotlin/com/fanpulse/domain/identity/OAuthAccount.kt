package com.fanpulse.domain.identity

import java.time.Instant
import java.util.UUID

/**
 * OAuth 계정 Entity
 *
 * 사용자의 소셜 로그인 계정 연동 정보를 관리합니다.
 */
data class OAuthAccount(
    val id: UUID,
    val userId: UUID,
    val provider: OAuthProvider,
    val providerUserId: String,
    val email: String,
    val emailVerified: Boolean,  // OAuth 제공자의 이메일 검증 상태
    val createdAt: Instant
) {
    companion object {
        /**
         * 새로운 OAuth 계정 연동 생성
         */
        fun create(
            userId: UUID,
            provider: OAuthProvider,
            providerUserId: String,
            email: String,
            emailVerified: Boolean  // 검증 상태 필수
        ): OAuthAccount {
            require(providerUserId.isNotBlank()) { "Provider user ID cannot be blank" }
            require(email.isNotBlank()) { "Email cannot be blank" }
            return OAuthAccount(
                id = UUID.randomUUID(),
                userId = userId,
                provider = provider,
                providerUserId = providerUserId,
                email = email,
                emailVerified = emailVerified,
                createdAt = Instant.now()
            )
        }
    }
}
