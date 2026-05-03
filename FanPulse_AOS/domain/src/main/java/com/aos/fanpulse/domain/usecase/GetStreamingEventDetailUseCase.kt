package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.StreamingBaseResponse
import com.aos.fanpulse.domain.model.StreamingEventDetail
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import javax.inject.Inject

class GetStreamingEventDetailUseCase @Inject constructor(
    private val repository: StreamingEventsRepository
) {
    /**
     * @param id 스트리밍 이벤트의 고유 ID (UUID)
     */
    suspend operator fun invoke(
        id: String
    ): Result<StreamingBaseResponse<StreamingEventDetail>> = runCatching {
        if (id.isBlank()) throw IllegalArgumentException("유효하지 않은 이벤트 ID입니다.")
        repository.getStreamingEventById(id)
    }
}