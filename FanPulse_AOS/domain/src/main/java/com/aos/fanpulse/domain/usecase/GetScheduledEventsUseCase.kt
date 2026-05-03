package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.StreamingEventSimpleItem
import com.aos.fanpulse.domain.model.StreamingPageResponse
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import javax.inject.Inject

class GetScheduledEventsUseCase @Inject constructor(
    private val streamingEventsRepository: StreamingEventsRepository
) {
    suspend operator fun invoke(
    ): Result<StreamingPageResponse<StreamingEventSimpleItem>> = runCatching {
        streamingEventsRepository.getScheduledEvents()
    }
}