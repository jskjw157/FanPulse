package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.StreamingBaseResponse
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import retrofit2.Response
import javax.inject.Inject

class GetStreamingEventDetailUseCase @Inject constructor(
    private val repository: StreamingEventsRepository
) {
    /**
     * @param id 스트리밍 이벤트의 고유 ID (UUID)
     */
    suspend operator fun invoke(
        id: String
    ): Response<StreamingBaseResponse<StreamingEventDetail>> {

        // 1. 유효성 검사: ID가 비어있으면 호출하지 않음
        if (id.isBlank()) {
            throw IllegalArgumentException("유효하지 않은 이벤트 ID입니다.")
        }

        // 2. Repository의 상세 조회 함수 호출
        return repository.getStreamingEventById(id)
    }
}