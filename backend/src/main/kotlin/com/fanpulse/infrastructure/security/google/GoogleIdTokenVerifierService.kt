package com.fanpulse.infrastructure.security.google

import com.fanpulse.domain.identity.OAuthProvider
import com.fanpulse.domain.identity.port.OAuthTokenVerifierPort
import com.fanpulse.domain.identity.port.OAuthUserInfo
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Google ID Token 검증 Adapter
 *
 * OAuthTokenVerifierPort 인터페이스를 구현하여
 * 아키텍처 의존성 방향 준수 (Infrastructure -> Domain)
 */
@Service
class GoogleIdTokenVerifierService(
    @Value("\${fanpulse.google.client-id}")
    private val clientId: String
) : OAuthTokenVerifierPort {

    private val verifier: GoogleIdTokenVerifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(clientId))
            .build()
    }

    override fun supportedProvider(): OAuthProvider = OAuthProvider.GOOGLE

    /**
     * Google ID Token 검증 및 사용자 정보 추출
     *
     * 보안 강화:
     * - subject (sub) null 체크
     * - email null 체크
     * - email_verified 필드 추출
     */
    override fun verify(idToken: String): OAuthUserInfo? {
        return try {
            val googleIdToken: GoogleIdToken? = verifier.verify(idToken)
            googleIdToken?.let { token ->
                val payload = token.payload

                // Null Safety 강화
                val subject = payload.subject
                val email = payload.email

                if (subject.isNullOrBlank()) {
                    logger.warn { "Google token missing subject (sub)" }
                    return null
                }

                if (email.isNullOrBlank()) {
                    logger.warn { "Google token missing email" }
                    return null
                }

                // email_verified 필드 추출 (기본값 false)
                val emailVerified = payload.emailVerified ?: false

                OAuthUserInfo(
                    provider = OAuthProvider.GOOGLE,
                    providerUserId = subject,
                    email = email,
                    emailVerified = emailVerified,
                    name = payload["name"] as? String,
                    pictureUrl = payload["picture"] as? String
                )
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to verify Google ID token" }
            null
        }
    }
}
