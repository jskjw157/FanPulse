package com.fanpulse.interfaces.rest.error

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.net.URI
import java.time.Instant

/**
 * RFC 7807 Problem Details for HTTP APIs.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7807">RFC 7807</a>
 */
@Schema(description = "RFC 7807 Problem Details response")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProblemDetail(
    /**
     * A URI reference that identifies the problem type.
     * Example: "https://api.fanpulse.com/errors/email-already-exists"
     */
    @Schema(
        description = "URI reference identifying the problem type",
        example = "https://api.fanpulse.com/errors/email-already-exists"
    )
    val type: URI = URI.create("about:blank"),

    /**
     * A short, human-readable summary of the problem type.
     */
    @Schema(
        description = "Short summary of the problem",
        example = "Email Already Exists"
    )
    val title: String,

    /**
     * The HTTP status code.
     */
    @Schema(
        description = "HTTP status code",
        example = "409"
    )
    val status: Int,

    /**
     * A human-readable explanation specific to this occurrence.
     */
    @Schema(
        description = "Detailed explanation of the problem",
        example = "The email 'user@example.com' is already registered"
    )
    val detail: String? = null,

    /**
     * A URI reference that identifies the specific occurrence.
     * Typically the request path.
     */
    @Schema(
        description = "URI of the request that caused the problem",
        example = "/api/v1/auth/register"
    )
    val instance: URI? = null,

    // === Extension fields (FanPulse specific) ===

    /**
     * Timestamp when the error occurred.
     */
    @Schema(
        description = "Timestamp of the error occurrence",
        example = "2026-01-19T22:10:00Z"
    )
    val timestamp: Instant = Instant.now(),

    /**
     * Machine-readable error code for client-side handling.
     */
    @Schema(
        description = "Machine-readable error code",
        example = "EMAIL_ALREADY_EXISTS"
    )
    @JsonProperty("errorCode")
    val errorCode: String? = null,

    /**
     * Field-level validation errors.
     */
    @Schema(description = "List of field-level validation errors")
    val errors: List<ApiFieldError>? = null,

    /**
     * Trace ID for distributed tracing (optional).
     */
    @Schema(
        description = "Trace ID for debugging",
        example = "abc123-def456"
    )
    val traceId: String? = null
) {
    companion object {
        private const val BASE_TYPE_URI = "https://api.fanpulse.com/errors"

        /**
         * Factory method for creating ProblemDetail with type URI.
         */
        fun of(
            errorType: ErrorType,
            detail: String? = null,
            instance: String? = null,
            errors: List<ApiFieldError>? = null,
            traceId: String? = null
        ): ProblemDetail = ProblemDetail(
            type = URI.create("$BASE_TYPE_URI/${errorType.slug}"),
            title = errorType.title,
            status = errorType.status,
            detail = detail,
            instance = instance?.let { URI.create(it) },
            errorCode = errorType.code,
            errors = errors,
            traceId = traceId
        )
    }
}
