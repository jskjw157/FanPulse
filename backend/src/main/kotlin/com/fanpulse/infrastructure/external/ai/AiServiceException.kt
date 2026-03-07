package com.fanpulse.infrastructure.external.ai

/**
 * Exception thrown when the Django AI Sidecar service returns an unexpected response.
 *
 * This includes cases such as empty responses, malformed JSON, or connection failures
 * that are not covered by [org.springframework.web.reactive.function.client.WebClientResponseException].
 *
 * Circuit Breaker will record this exception as a failure.
 */
class AiServiceException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
