package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.LastFmArtist
import com.aos.fanpulse.domain.repository.MusicRepository
import javax.inject.Inject

class SearchLastFmArtistUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(name: String): Result<List<LastFmArtist>> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("검색어를 입력해주세요."))
        }
        return repository.searchArtist(name)
    }
}