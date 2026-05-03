package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ChannelDiscoverResponse
import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import javax.inject.Inject

class DiscoverAndSyncChannelsUseCase@Inject constructor(
    private val repository: ArtistChannelsRepository
) {
    suspend operator fun invoke(): Result<ChannelDiscoverResponse> {
        // runCatching을 통해 리포지토리에서 던지는 Exception을 Result.failure로 변환합니다.
        return runCatching {
            // 1. 서버에 새로운 채널 발견(Discover) 요청
            // 리포지토리는 이제 Response가 아닌 순수 모델을 반환하며, 실패 시 Exception을 던집니다.
            val discoverData = repository.discoverChannels()

            // 2. 동기화(Sync) 로직 실행
            // [여기에 추가적인 비즈니스 로직이 들어갑니다]
            // 예: 특정 조건에 맞는 채널만 필터링하거나, 로컬 DB(Room) 작업 UseCase를 여기서 호출할 수 있습니다.

            discoverData
        }
    }
}