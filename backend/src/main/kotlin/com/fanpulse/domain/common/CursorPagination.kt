package com.fanpulse.domain.common

import java.time.Instant
import java.util.Base64
import java.util.UUID

/**
 * Cursor-based pagination request.
 *
 * Framework-independent domain model for cursor pagination that represents a request to fetch
 * a page of data using cursor-based pagination strategy.
 *
 * Cursor-based pagination is preferred over offset-based pagination for APIs because it:
 * - Remains stable when items are added/deleted before the cursor position
 * - Enables efficient database queries using indexed composite keys
 * - Prevents "gaps" or duplicates when sorting by dynamic fields (e.g., viewerCount)
 *
 * @property limit Number of items to fetch (must be between 1 and 100, inclusive)
 * @property cursor Decoded cursor from previous response, or null for first page
 *
 * @throws IllegalArgumentException if limit <= 0 or limit > 100
 *
 * @see DecodedCursor
 */
data class CursorPageRequest(
    val limit: Int,
    val cursor: DecodedCursor?
) {
    init {
        require(limit > 0) { "Limit must be positive" }
        require(limit <= 100) { "Limit cannot exceed 100" }
    }

    companion object {
        /**
         * Creates a [CursorPageRequest] from limit and encoded cursor string.
         *
         * @param limit Number of items to fetch (will be validated by constructor)
         * @param cursorString Base64-encoded cursor string from previous response, or null for first page
         * @return Parsed [CursorPageRequest] with decoded cursor
         * @throws IllegalArgumentException if cursor string is invalid or limit is out of range
         */
        fun of(limit: Int, cursorString: String?): CursorPageRequest {
            val decoded = cursorString?.let { DecodedCursor.decode(it) }
            return CursorPageRequest(limit, decoded)
        }
    }
}

/**
 * Decoded cursor containing pagination state.
 *
 * Represents a position in a paginated dataset using a composite key of (scheduledAt, id),
 * which enables stable, index-efficient cursor pagination across streaming events.
 *
 * Design rationale:
 * - **scheduledAt**: Primary sort field, allows efficient database queries with index on (scheduledAt, id)
 * - **id**: Secondary sort field, ensures total ordering when scheduledAt values are equal
 * - **Composite key**: Prevents gaps/duplicates when events are inserted before the cursor position
 *
 * The cursor is serialized as Base64-encoded JSON for safe transmission in URLs:
 * Example: `eyJzY2hlZHVsZWRBdCI6MTcwNDAwMDAwMDAwMCwiaWQiOiI1NjMzY2RmZi0xNjEzLTQxNjMtYTMwNS04YzE0YTUxNDMyMjEifQ==`
 *
 * @property scheduledAt Unix timestamp (epoch milliseconds) of the event's scheduled time
 * @property id String representation of the event's UUID
 *
 * @see CursorPageRequest
 * @see CursorPageResult
 */
data class DecodedCursor(
    val scheduledAt: Long,  // epoch millis
    val id: String          // UUID as string
) {
    /**
     * Encodes this cursor to a Base64-encoded JSON string safe for URL transmission.
     *
     * The encoded cursor can be passed as a query parameter to subsequent API requests.
     * See [decode] for the inverse operation.
     *
     * @return Base64-encoded JSON string containing scheduledAt and id
     */
    fun encode(): String {
        val json = """{"scheduledAt":$scheduledAt,"id":"$id"}"""
        return Base64.getUrlEncoder().encodeToString(json.toByteArray())
    }

    companion object {
        /**
         * Decodes a Base64-encoded JSON cursor string to a [DecodedCursor] object.
         *
         * The input string should be in the format produced by [encode].
         * Invalid or malformed cursors will throw IllegalArgumentException.
         *
         * @param encoded Base64-encoded cursor string from API response
         * @return Decoded cursor object
         * @throws IllegalArgumentException if the encoded string is not valid Base64 or missing required fields
         */
        fun decode(encoded: String): DecodedCursor {
            return try {
                val json = String(Base64.getUrlDecoder().decode(encoded))

                // Parse JSON and extract scheduledAt and id
                val scheduledAt = Regex(""""scheduledAt":(\d+)""")
                    .find(json)?.groupValues?.get(1)?.toLong()
                    ?: throw IllegalArgumentException("Invalid cursor: missing scheduledAt")

                val id = Regex(""""id":"([^"]+)"""")
                    .find(json)?.groupValues?.get(1)
                    ?: throw IllegalArgumentException("Invalid cursor: missing id")

                DecodedCursor(scheduledAt, id)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid cursor format", e)
            }
        }

        /**
         * Creates a [DecodedCursor] from instant and UUID components.
         *
         * Convenience factory method for creating cursors from domain objects without
         * manual timestamp conversion.
         *
         * @param scheduledAt Event's scheduled time as [Instant]
         * @param id Event's unique identifier as [UUID]
         * @return Decoded cursor containing converted values
         */
        fun from(scheduledAt: Instant, id: UUID): DecodedCursor {
            return DecodedCursor(scheduledAt.toEpochMilli(), id.toString())
        }
    }
}

/**
 * Cursor-based pagination result containing a page of items.
 *
 * This is the response type returned by cursor-based pagination queries. It contains the items
 * for the current page along with metadata required to fetch the next page.
 *
 * Design details:
 * - **items**: Exactly [limit] items (or fewer if this is the last page)
 * - **nextCursor**: Encoded cursor to use in the next request (null if no more pages)
 * - **hasMore**: Flag indicating whether more pages exist beyond nextCursor
 *
 * The limit+1 query strategy: The repository fetches limit+1 items to determine if hasMore
 * should be true; if exactly limit+1 items are returned, the last item becomes the nextCursor
 * and is excluded from the items list.
 *
 * @property items List of page items (T can be any domain or DTO type)
 * @property nextCursor Base64-encoded cursor for the next page, or null if no more pages
 * @property hasMore Whether additional pages exist beyond the current page
 *
 * @see CursorPageRequest
 */
data class CursorPageResult<T>(
    val items: List<T>,
    val nextCursor: String?,
    val hasMore: Boolean
) {
    companion object {
        /**
         * Creates an empty cursor page result.
         *
         * Useful for returning empty results when no items match the query criteria.
         *
         * @return Empty [CursorPageResult] with no items, no next cursor, and hasMore=false
         */
        fun <T> empty(): CursorPageResult<T> = CursorPageResult(
            items = emptyList(),
            nextCursor = null,
            hasMore = false
        )
    }
}
