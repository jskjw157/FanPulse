package com.fanpulse.application.service.search

/**
 * Search service related exceptions.
 */
open class SearchException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when search service encounters a data access error.
 */
class SearchServiceException(
    message: String = "Search service temporarily unavailable",
    cause: Throwable? = null
) : SearchException(message, cause)
