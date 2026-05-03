package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.StreamingEventSimpleItem
import com.aos.fanpulse.domain.model.StreamingPageResponse
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import javax.inject.Inject

class GetLiveEventsUseCase @Inject constructor(
    private val streamingEventsRepository: StreamingEventsRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 20
    ): Result<StreamingPageResponse<StreamingEventSimpleItem>> = runCatching {

        streamingEventsRepository.getLiveEvents(
            page = page,
            size = size
        )
    }
}