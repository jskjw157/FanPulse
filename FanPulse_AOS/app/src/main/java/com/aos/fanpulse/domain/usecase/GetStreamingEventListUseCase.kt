package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import retrofit2.Response
import javax.inject.Inject
import kotlin.Int
import kotlin.String

class GetStreamingEventListUseCase @Inject constructor(
    private val repository: StreamingEventsRepository
) {
    enum class FetchType { CURSOR, SCHEDULED, LIVE, LEGACY, ARTIST }

    suspend operator fun invoke(
        type: FetchType,
        status: String? = null,
        artistId: String? = null,
        page: Int = 0,
        cursor: String? = null,
        platform: String? = "YOUTUBE",
        scheduledAfter: String? = null,
        scheduledBefore: String? = null,
        size: Int = 20,
        sortBy: String = "scheduledAt",
        sortDir: String = "desc"
    ): Response<*> {
        return when (type) {
            FetchType.CURSOR -> repository.getStreamingEvents(status, cursor = cursor)
            FetchType.LIVE -> repository.getLiveEvents(page)
            FetchType.ARTIST -> repository.getArtistEvents(artistId!!, page)
            FetchType.LEGACY -> repository.getLegacyEvents(status, platform, artistId, scheduledAfter, scheduledBefore, page, size, sortBy, sortDir)
            FetchType.SCHEDULED -> repository.getScheduledEvents(page, size)
        }
    }
}