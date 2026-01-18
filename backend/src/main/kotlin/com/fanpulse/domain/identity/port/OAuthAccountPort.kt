package com.fanpulse.domain.identity.port

import com.fanpulse.domain.identity.OAuthAccount
import com.fanpulse.domain.identity.OAuthProvider
import java.util.UUID

/**
 * OAuthAccount Repository Port (출력 포트)
 *
 * OAuth 계정 저장소 인터페이스
 */
interface OAuthAccountPort {
    /**
     * OAuth 계정 저장 (Race Condition 시 예외 발생)
     * @throws com.fanpulse.application.service.identity.OAuthAccountAlreadyExistsException 중복 시
     */
    fun save(oAuthAccount: OAuthAccount): OAuthAccount

    /**
     * 중복 체크 후 저장 (원자적 연산)
     * INSERT ... ON CONFLICT DO NOTHING 사용
     * @return 저장된 계정 (이미 존재하면 null)
     */
    fun saveIfNotExists(oAuthAccount: OAuthAccount): OAuthAccount?

    /**
     * ID로 OAuth 계정 조회
     */
    fun findById(id: UUID): OAuthAccount?

    /**
     * 제공자와 제공자 사용자 ID로 OAuth 계정 조회
     */
    fun findByProviderAndProviderUserId(
        provider: OAuthProvider,
        providerUserId: String
    ): OAuthAccount?

    /**
     * 사용자 ID로 OAuth 계정 목록 조회
     */
    fun findByUserId(userId: UUID): List<OAuthAccount>
}
