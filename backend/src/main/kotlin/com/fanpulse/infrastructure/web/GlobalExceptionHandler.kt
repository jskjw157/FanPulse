package com.fanpulse.infrastructure.web

import com.fanpulse.application.service.identity.*
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

data class ErrorResponse(
    val success: Boolean = false,
    val error: ErrorDetail
)

data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.associate {
            val fieldName = (it as FieldError).field
            val errorMessage = it.defaultMessage ?: "Invalid value"
            fieldName to errorMessage
        }

        val response = ErrorResponse(
            error = ErrorDetail(
                code = "VALIDATION_ERROR",
                message = "Validation failed",
                details = errors
            )
        )

        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn { "Email already exists: ${ex.message}" }
        val response = ErrorResponse(
            error = ErrorDetail(
                code = "AUTH_EMAIL_EXISTS",
                message = ex.message ?: "Email already exists"
            )
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(UsernameAlreadyExistsException::class)
    fun handleUsernameAlreadyExists(ex: UsernameAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn { "Username already exists: ${ex.message}" }
        val response = ErrorResponse(
            error = ErrorDetail(
                code = "AUTH_USERNAME_EXISTS",
                message = ex.message ?: "Username already exists"
            )
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        logger.warn { "Invalid credentials: ${ex.message}" }
        val response = ErrorResponse(
            error = ErrorDetail(
                code = "AUTH_INVALID_CREDENTIALS",
                message = ex.message ?: "Invalid email or password"
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(InvalidGoogleTokenException::class)
    fun handleInvalidGoogleToken(ex: InvalidGoogleTokenException): ResponseEntity<ErrorResponse> {
        logger.warn { "Invalid Google token: ${ex.message}" }
        val response = ErrorResponse(
            error = ErrorDetail(
                code = "AUTH_GOOGLE_FAILED",
                message = ex.message ?: "Invalid or expired Google token"
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<ErrorResponse> {
        logger.warn { "Invalid token: ${ex.message}" }
        val response = ErrorResponse(
            error = ErrorDetail(
                code = "AUTH_TOKEN_INVALID",
                message = ex.message ?: "Invalid or expired token"
            )
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn { "User not found: ${ex.message}" }
        val response = ErrorResponse(
            error = ErrorDetail(
                code = "NOT_FOUND",
                message = ex.message ?: "User not found"
            )
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "Unexpected error occurred" }
        val response = ErrorResponse(
            error = ErrorDetail(
                code = "SERVER_ERROR",
                message = "An unexpected error occurred"
            )
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
