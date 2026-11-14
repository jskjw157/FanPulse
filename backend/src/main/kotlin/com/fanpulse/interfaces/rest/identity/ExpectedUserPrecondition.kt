package com.fanpulse.interfaces.rest.identity

import com.fanpulse.interfaces.rest.error.ErrorType
import jakarta.servlet.http.HttpServletRequest
import java.util.UUID

class ExpectedUserPreconditionException(val errorType: ErrorType) : RuntimeException()

/** An identity assertion, never an alternate source of authentication or a target user ID. */
fun requireExpectedUser(request: HttpServletRequest, userId: UUID, cookieWrite: Boolean = false) {
    val expected = request.getHeader("X-Expected-User-Id")
    // Match JwtAuthenticationFilter's credential precedence. Native bearer clients stay compatible.
    val usesBearer = request.getHeader("Authorization")?.startsWith("Bearer ") == true
    if (expected == null) {
        if (cookieWrite && !usesBearer) {
            throw ExpectedUserPreconditionException(ErrorType.EXPECTED_USER_REQUIRED)
        }
        return
    }
    // String comparison also rejects malformed/non-canonical UUIDs and multi-value headers.
    if (request.getHeaders("X-Expected-User-Id").toList().size != 1 || !expected.equals(userId.toString(), ignoreCase = true)) {
        throw ExpectedUserPreconditionException(ErrorType.EXPECTED_USER_MISMATCH)
    }
}
