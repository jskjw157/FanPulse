package com.fanpulse.interfaces.rest.error

import org.springframework.http.HttpStatus

/**
 * Enumeration of all error types with RFC 7807 mappings.
 *
 * @property slug URL-friendly identifier (used in type URI)
 * @property title Human-readable title
 * @property status HTTP status code
 * @property code Machine-readable error code
 */
enum class ErrorType(
    val slug: String,
    val title: String,
    val status: Int,
    val code: String
) {
    // === Authentication Errors (401) ===
    INVALID_CREDENTIALS(
        slug = "invalid-credentials",
        title = "Invalid Credentials",
        status = HttpStatus.UNAUTHORIZED.value(),
        code = "INVALID_CREDENTIALS"
    ),
    INVALID_TOKEN(
        slug = "invalid-token",
        title = "Invalid Token",
        status = HttpStatus.UNAUTHORIZED.value(),
        code = "INVALID_TOKEN"
    ),
    TOKEN_EXPIRED(
        slug = "token-expired",
        title = "Token Expired",
        status = HttpStatus.UNAUTHORIZED.value(),
        code = "TOKEN_EXPIRED"
    ),
    REFRESH_TOKEN_REUSED(
        slug = "refresh-token-reused",
        title = "Refresh Token Reused",
        status = HttpStatus.UNAUTHORIZED.value(),
        code = "REFRESH_TOKEN_REUSED"
    ),
    INVALID_GOOGLE_TOKEN(
        slug = "invalid-google-token",
        title = "Invalid Google Token",
        status = HttpStatus.UNAUTHORIZED.value(),
        code = "INVALID_GOOGLE_TOKEN"
    ),
    OAUTH_EMAIL_NOT_VERIFIED(
        slug = "oauth-email-not-verified",
        title = "OAuth Email Not Verified",
        status = HttpStatus.BAD_REQUEST.value(),
        code = "OAUTH_EMAIL_NOT_VERIFIED"
    ),

    // === Rate Limiting Errors (429) ===
    RATE_LIMIT_EXCEEDED(
        slug = "rate-limit-exceeded",
        title = "Too Many Requests",
        status = 429,  // HttpStatus.TOO_MANY_REQUESTS
        code = "RATE_LIMIT_EXCEEDED"
    ),

    // === Validation Errors (400) ===
    VALIDATION_FAILED(
        slug = "validation-failed",
        title = "Validation Failed",
        status = HttpStatus.BAD_REQUEST.value(),
        code = "VALIDATION_FAILED"
    ),
    INVALID_REQUEST(
        slug = "invalid-request",
        title = "Invalid Request",
        status = HttpStatus.BAD_REQUEST.value(),
        code = "INVALID_REQUEST"
    ),
    INVALID_PASSWORD(
        slug = "invalid-password",
        title = "Invalid Password",
        status = HttpStatus.BAD_REQUEST.value(),
        code = "INVALID_PASSWORD"
    ),

    // === Not Found Errors (404) ===
    USER_NOT_FOUND(
        slug = "user-not-found",
        title = "User Not Found",
        status = HttpStatus.NOT_FOUND.value(),
        code = "USER_NOT_FOUND"
    ),
    RESOURCE_NOT_FOUND(
        slug = "resource-not-found",
        title = "Resource Not Found",
        status = HttpStatus.NOT_FOUND.value(),
        code = "RESOURCE_NOT_FOUND"
    ),

    // === Conflict Errors (409) ===
    EMAIL_ALREADY_EXISTS(
        slug = "email-already-exists",
        title = "Email Already Exists",
        status = HttpStatus.CONFLICT.value(),
        code = "EMAIL_ALREADY_EXISTS"
    ),
    USERNAME_ALREADY_EXISTS(
        slug = "username-already-exists",
        title = "Username Already Exists",
        status = HttpStatus.CONFLICT.value(),
        code = "USERNAME_ALREADY_EXISTS"
    ),

    // === Service Unavailable Errors (503) ===
    SEARCH_SERVICE_UNAVAILABLE(
        slug = "search-service-unavailable",
        title = "Search Service Unavailable",
        status = HttpStatus.SERVICE_UNAVAILABLE.value(),
        code = "SEARCH_SERVICE_UNAVAILABLE"
    ),

    // === Server Errors (500) ===
    INTERNAL_ERROR(
        slug = "internal-error",
        title = "Internal Server Error",
        status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
        code = "INTERNAL_ERROR"
    );

    /**
     * Create ProblemDetail from this ErrorType.
     */
    fun toProblemDetail(
        detail: String? = null,
        instance: String? = null,
        errors: List<ApiFieldError>? = null
    ): ProblemDetail = ProblemDetail.of(
        errorType = this,
        detail = detail,
        instance = instance,
        errors = errors
    )
}
