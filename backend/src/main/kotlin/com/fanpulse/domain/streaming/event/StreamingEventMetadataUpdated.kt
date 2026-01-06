package com.fanpulse.domain.streaming.event

import com.fanpulse.domain.common.AbstractDomainEvent
import java.time.Instant
import java.util.*

/**
 * Domain event raised when streaming event metadata is updated from YouTube.
 */
data class StreamingEventMetadataUpdated(
    /**
     * ID of the streaming event whose metadata was updated.
     */
    val streamingEventId: UUID,

    /**
     * Previous title before update.
     */
    val previousTitle: String,

    /**
     * New title after update.
     */
    val newTitle: String,

    /**
     * Previous thumbnail URL before update.
     */
    val previousThumbnailUrl: String?,

    /**
     * New thumbnail URL after update.
     */
    val newThumbnailUrl: String?,

    /**
     * Whether the title was changed.
     */
    val titleChanged: Boolean = previousTitle != newTitle,

    /**
     * Whether the thumbnail was changed.
     */
    val thumbnailChanged: Boolean = previousThumbnailUrl != newThumbnailUrl,

    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : AbstractDomainEvent(eventId, occurredAt) {

    /**
     * Returns true if any metadata was actually changed.
     */
    fun hasChanges(): Boolean = titleChanged || thumbnailChanged
}
