package com.fanpulse.infrastructure.external

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
 * Google ID Token verifier implementation.
 *
 * Verifies Google ID tokens received from client-side Google Sign-In.
 * Uses Google's official library for cryptographic verification.
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

    override fun verify(idToken: String): OAuthUserInfo? {
        return try {
            val googleIdToken: GoogleIdToken? = verifier.verify(idToken)

            if (googleIdToken == null) {
                logger.debug { "Google ID token verification failed: token is invalid" }
                return null
            }

            val payload = googleIdToken.payload
            val userId = payload.subject
            val email = payload.email

            if (userId == null || email == null) {
                logger.warn { "Google ID token missing required claims: userId=$userId, email=$email" }
                return null
            }

            val emailVerified = payload.emailVerified ?: false
            val name = payload["name"] as? String
            val pictureUrl = payload["picture"] as? String

            logger.debug { "Google ID token verified successfully: userId=$userId, email=$email" }

            OAuthUserInfo(
                providerUserId = userId,
                email = email,
                emailVerified = emailVerified,
                name = name,
                pictureUrl = pictureUrl
            )
        } catch (e: Exception) {
            logger.error(e) { "Google ID token verification error: ${e.message}" }
            null
        }
    }
}
