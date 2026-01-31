package com.fanpulse.interfaces.rest.error

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Field-level error details for validation failures.
 */
@Schema(description = "Field-level validation error")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiFieldError(
    /**
     * The field name that failed validation.
     */
    @Schema(
        description = "Field name",
        example = "email"
    )
    val field: String,

    /**
     * Machine-readable error code.
     */
    @Schema(
        description = "Error code",
        example = "invalid_format"
    )
    val code: String,

    /**
     * Human-readable error message.
     */
    @Schema(
        description = "Error message",
        example = "Invalid email format"
    )
    val message: String,

    /**
     * The rejected value (sanitized for security).
     */
    @Schema(
        description = "Rejected value",
        example = "invalid-email"
    )
    val rejectedValue: Any? = null
) {
    companion object {
        /**
         * Create from Spring's FieldError.
         */
        fun from(fieldError: org.springframework.validation.FieldError): ApiFieldError {
            return ApiFieldError(
                field = fieldError.field,
                code = fieldError.code?.toSnakeCase() ?: "invalid",
                message = fieldError.defaultMessage ?: "Validation failed",
                rejectedValue = sanitizeValue(fieldError.rejectedValue)
            )
        }

        /**
         * Sanitize sensitive values (passwords, tokens, etc.)
         */
        private fun sanitizeValue(value: Any?): Any? {
            return when {
                value == null -> null
                value is String && value.length > 100 -> "${value.take(50)}...[truncated]"
                else -> value
            }
        }

        /**
         * Convert camelCase to snake_case.
         */
        private fun String.toSnakeCase(): String {
            return this.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
        }
    }
}
