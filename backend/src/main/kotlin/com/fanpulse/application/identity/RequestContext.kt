package com.fanpulse.application.identity

/**
 * Request context containing client information.
 *
 * Used for security audit trails (login tracking, suspicious activity detection).
 */
data class RequestContext(
    val ipAddress: String?,
    val userAgent: String?
) {
    companion object {
        /**
         * Creates an empty RequestContext when client information is unavailable.
         */
        fun empty() = RequestContext(ipAddress = null, userAgent = null)
    }
}
