package com.fanpulse.domain.identity

import java.time.Instant
import java.util.UUID

/**
 * User Aggregate Root
 *
 * 사용자 인증 및 프로필 정보를 관리하는 집합 루트입니다.
 */
data class User(
    val id: UUID,
    val username: Username,
    val email: Email,
    val passwordHash: String?,  // OAuth 전용 사용자는 null
    val emailVerified: Boolean,  // 이메일 검증 여부
    val createdAt: Instant
) {
    companion object {
        /**
         * 이메일/비밀번호로 회원가입 (이메일 미검증 상태)
         */
        fun register(
            email: Email,
            username: Username,
            passwordHash: String
        ): User {
            require(passwordHash.isNotBlank()) { "Password hash cannot be blank" }
            return User(
                id = UUID.randomUUID(),
                username = username,
                email = email,
                passwordHash = passwordHash,
                emailVerified = false,  // 이메일 미검증
                createdAt = Instant.now()
            )
        }

        /**
         * OAuth로 회원가입 (이메일 검증됨)
         */
        fun registerWithOAuth(
            email: Email,
            username: Username
        ): User {
            return User(
                id = UUID.randomUUID(),
                username = username,
                email = email,
                passwordHash = null,
                emailVerified = true,  // OAuth는 검증된 이메일
                createdAt = Instant.now()
            )
        }
    }

    /**
     * 비밀번호가 설정되어 있는지 확인
     */
    fun hasPassword(): Boolean = passwordHash != null

    /**
     * 비밀번호 설정 (OAuth 사용자가 나중에 비밀번호 설정 시)
     */
    fun setPassword(newPasswordHash: String): User {
        require(newPasswordHash.isNotBlank()) { "Password hash cannot be blank" }
        return copy(passwordHash = newPasswordHash)
    }

    /**
     * 비밀번호 변경
     */
    fun changePassword(newPasswordHash: String): User {
        require(hasPassword()) { "Cannot change password for OAuth-only user" }
        require(newPasswordHash.isNotBlank()) { "Password hash cannot be blank" }
        return copy(passwordHash = newPasswordHash)
    }

    /**
     * 이메일 검증 완료 처리
     */
    fun verifyEmail(): User = copy(emailVerified = true)

    /**
     * OAuth 계정 연결 가능 여부 확인
     * 이메일이 검증된 계정만 OAuth 연결 가능
     */
    fun canLinkOAuthAccount(): Boolean = emailVerified
}
