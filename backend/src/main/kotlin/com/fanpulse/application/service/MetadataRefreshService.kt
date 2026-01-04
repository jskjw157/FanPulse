package com.fanpulse.application.service

import java.util.*

/**
 * Service for refreshing YouTube streaming event metadata.
 */
interface MetadataRefreshService {

    /**
     * Refresh metadata for all LIVE events.
     * Called hourly by scheduler.
     *
     * @return RefreshResult with statistics
     */
    fun refreshLiveEvents(): RefreshResult

    /**
     * Refresh metadata for all non-ENDED events.
     * Called daily by scheduler.
     *
     * @return RefreshResult with statistics
     */
    fun refreshAllEvents(): RefreshResult

    /**
     * Refresh metadata for a single event.
     *
     * @param eventId UUID of the streaming event
     * @return true if refresh was successful, false otherwise
     */
    fun refreshEvent(eventId: UUID): Boolean
}

/**
 * Result of a batch refresh operation.
 */
data class RefreshResult(
    val total: Int,
    val updated: Int,
    val failed: Int,
    val errors: List<RefreshError> = emptyList()
)

/**
 * Error information for a failed refresh.
 */
data class RefreshError(
    val eventId: UUID,
    val reason: String
)
