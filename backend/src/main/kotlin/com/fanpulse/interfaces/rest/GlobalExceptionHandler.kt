package com.fanpulse.interfaces.rest

import com.fanpulse.application.identity.*
import com.fanpulse.interfaces.rest.error.*
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest

private val logger = KotlinLogging.logger {}

/**
 * Global exception handler implementing RFC 7807 Problem Details.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7807">RFC 7807</a>
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    // === Authentication Exceptions ===

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(
        ex: InvalidCredentialsException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Invalid credentials attempt" }
        return createResponse(
            ErrorType.INVALID_CREDENTIALS,
            detail = "The provided email or password is incorrect",
            request = request
        )
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(
        ex: InvalidTokenException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Invalid token: ${ex.message}" }
        return createResponse(
            ErrorType.INVALID_TOKEN,
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPassword(
        ex: InvalidPasswordException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Invalid password: ${ex.message}" }
        return createResponse(
            ErrorType.INVALID_PASSWORD,
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(RefreshTokenReusedException::class)
    fun handleRefreshTokenReused(
        ex: RefreshTokenReusedException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.warn { "Refresh token reuse detected - potential security breach" }
        return createResponse(
            ErrorType.REFRESH_TOKEN_REUSED,
            detail = ex.message,
            request = request
        )
    }

    // === OAuth Exceptions ===

    @ExceptionHandler(InvalidGoogleTokenException::class)
    fun handleInvalidGoogleToken(
        ex: InvalidGoogleTokenException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Invalid Google token: ${ex.message}" }
        return createResponse(
            ErrorType.INVALID_GOOGLE_TOKEN,
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(OAuthEmailNotVerifiedException::class)
    fun handleOAuthEmailNotVerified(
        ex: OAuthEmailNotVerifiedException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "OAuth email not verified: ${ex.message}" }
        return createResponse(
            ErrorType.OAUTH_EMAIL_NOT_VERIFIED,
            detail = ex.message,
            request = request
        )
    }

    // === Conflict Exceptions ===

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(
        ex: EmailAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Email already exists: ${ex.email}" }
        return createResponse(
            ErrorType.EMAIL_ALREADY_EXISTS,
            detail = "The email '${ex.email}' is already registered",
            request = request,
            errors = listOf(
                ApiFieldError(
                    field = "email",
                    code = "already_exists",
                    message = "Email already exists",
                    rejectedValue = ex.email
                )
            )
        )
    }

    @ExceptionHandler(UsernameAlreadyExistsException::class)
    fun handleUsernameAlreadyExists(
        ex: UsernameAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Username already exists: ${ex.username}" }
        return createResponse(
            ErrorType.USERNAME_ALREADY_EXISTS,
            detail = "The username '${ex.username}' is already taken",
            request = request,
            errors = listOf(
                ApiFieldError(
                    field = "username",
                    code = "already_exists",
                    message = "Username already exists",
                    rejectedValue = ex.username
                )
            )
        )
    }

    // === Not Found Exceptions ===

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(
        ex: UserNotFoundException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "User not found: ${ex.message}" }
        return createResponse(
            ErrorType.USER_NOT_FOUND,
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(
        ex: NoSuchElementException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Resource not found: ${ex.message}" }
        return createResponse(
            ErrorType.RESOURCE_NOT_FOUND,
            detail = ex.message,
            request = request
        )
    }

    // === Validation Exceptions ===

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { ApiFieldError.from(it) }
        val errorCount = fieldErrors.size

        logger.debug { "Validation failed: $errorCount error(s)" }

        return createResponse(
            ErrorType.VALIDATION_FAILED,
            detail = "Request validation failed with $errorCount error(s)",
            request = request,
            errors = fieldErrors
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.debug { "Invalid request: ${ex.message}" }
        return createResponse(
            ErrorType.INVALID_REQUEST,
            detail = ex.message,
            request = request
        )
    }

    // === Fallback Handler ===

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ProblemDetail> {
        logger.error(ex) { "Unexpected error occurred" }
        return createResponse(
            ErrorType.INTERNAL_ERROR,
            detail = "An unexpected error occurred. Please try again later.",
            request = request
        )
    }

    // === Helper Methods ===

    private fun createResponse(
        errorType: ErrorType,
        detail: String? = null,
        request: WebRequest,
        errors: List<ApiFieldError>? = null
    ): ResponseEntity<ProblemDetail> {
        val instance = extractRequestUri(request)
        val traceId = extractTraceId(request)

        val problemDetail = ProblemDetail.of(
            errorType = errorType,
            detail = detail,
            instance = instance,
            errors = errors,
            traceId = traceId
        )

        return ResponseEntity
            .status(errorType.status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail)
    }

    private fun extractRequestUri(request: WebRequest): String? {
        return (request as? ServletWebRequest)?.request?.requestURI
    }

    private fun extractTraceId(request: WebRequest): String? {
        return (request as? ServletWebRequest)?.request?.getHeader("X-Trace-Id")
    }
}

// === Backward Compatibility (Deprecated) ===

/**
 * @deprecated Use ProblemDetail instead. Will be removed in v2.0.
 */
@Deprecated("Use ProblemDetail instead", replaceWith = ReplaceWith("ProblemDetail"))
data class ErrorResponse(
    val error: String
)

/**
 * @deprecated Use ProblemDetail instead. Will be removed in v2.0.
 */
@Deprecated("Use ProblemDetail instead", replaceWith = ReplaceWith("ProblemDetail"))
data class ValidationErrorResponse(
    val message: String,
    val errors: List<FieldError>
)

/**
 * @deprecated Use ApiFieldError instead. Will be removed in v2.0.
 */
@Deprecated("Use ApiFieldError instead", replaceWith = ReplaceWith("ApiFieldError"))
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: String?
)
