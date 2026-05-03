package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.StreamingBaseResponse
import com.aos.fanpulse.domain.model.StreamingEventCursorData
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import javax.inject.Inject

class GetStreamingEventsUseCase @Inject constructor(
    private val streamingEventsRepository: StreamingEventsRepository
) {
    suspend operator fun invoke(
    ): Result<StreamingBaseResponse<StreamingEventCursorData>> = runCatching {
        streamingEventsRepository.getStreamingEvents()
    }
}