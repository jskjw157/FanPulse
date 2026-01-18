package com.fanpulse.interfaces.rest

import com.fanpulse.application.identity.*
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

/**
 * Global exception handler for REST API.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.debug { "Email already exists: ${ex.email}" }
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(error = ex.message ?: "Email already exists"))
    }

    @ExceptionHandler(UsernameAlreadyExistsException::class)
    fun handleUsernameAlreadyExists(ex: UsernameAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.debug { "Username already exists: ${ex.username}" }
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(error = ex.message ?: "Username already exists"))
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        logger.debug { "Invalid credentials" }
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(error = ex.message ?: "Invalid credentials"))
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<ErrorResponse> {
        logger.debug { "Invalid token: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(error = ex.message ?: "Invalid token"))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.debug { "User not found: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(error = ex.message ?: "User not found"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.debug { "Bad request: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = ex.message ?: "Bad request"))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        logger.debug { "Resource not found: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(error = ex.message ?: "Resource not found"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { fieldError ->
            FieldError(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "Validation failed",
                rejectedValue = fieldError.rejectedValue?.toString()
            )
        }
        logger.debug { "Validation failed: ${errors.size} errors" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse(message = "Validation failed", errors = errors))
    }
}

/**
 * Error response DTO.
 */
data class ErrorResponse(
    val error: String
)

/**
 * Validation error response DTO.
 */
data class ValidationErrorResponse(
    val message: String,
    val errors: List<FieldError>
)

/**
 * Field error DTO.
 */
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: String?
)
