package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.StreamingEventsRepository
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
    ): Result<Any> {

        return runCatching {
            val safePage = if (page < 0) 0 else page
            val safeSize = if (size <= 0) 20 else size
            when (type) {
                FetchType.CURSOR ->
                    repository.getStreamingEvents(status, cursor = cursor)
                FetchType.LIVE ->
                    repository.getLiveEvents(safePage)
                FetchType.ARTIST -> {
                    if (artistId.isNullOrBlank()) throw IllegalArgumentException("아티스트 ID가 필요합니다.")
                    repository.getArtistEvents(artistId, safePage)
                }
                FetchType.LEGACY ->
                    repository.getLegacyEvents(status, platform, artistId, scheduledAfter, scheduledBefore, safePage, safeSize, sortBy, sortDir)
                FetchType.SCHEDULED ->
                    repository.getScheduledEvents(safePage, safeSize)
            }
        }
    }
}