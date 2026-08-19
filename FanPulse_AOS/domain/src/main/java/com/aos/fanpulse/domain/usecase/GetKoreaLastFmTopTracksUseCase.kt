package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ChartTrack
import com.aos.fanpulse.domain.repository.MusicRepository
import javax.inject.Inject

class GetKoreaLastFmTopTracksUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    /**
     * operator fun invoke를 통해 ViewModel에서 함수처럼 편리하게 호출할 수 있습니다.
     * 기본값으로 1페이지, 50개 조회를 세팅합니다.
     */
    suspend operator fun invoke(page: Int = 1, limit: Int = 50): Result<List<ChartTrack>> {
        val validPage = if (page < 1) 1 else page

        return repository.getKoreaTopTracks(validPage, limit)
    }
}