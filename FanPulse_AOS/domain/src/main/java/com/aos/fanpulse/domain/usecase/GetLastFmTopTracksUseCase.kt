package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ChartTrack
import com.aos.fanpulse.domain.repository.MusicRepository
import javax.inject.Inject

class GetLastFmTopTracksUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 50): Result<List<ChartTrack>> {
        val validPage = if (page < 1) 1 else page

        return repository.getTopTracks(validPage, limit)
    }
}