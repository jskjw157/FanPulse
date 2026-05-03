package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ArtistDetail
import com.aos.fanpulse.domain.repository.ArtistsRepository
import javax.inject.Inject

class GetArtistDetailUseCase @Inject constructor(
    private val artistsRepository: ArtistsRepository
) {
    suspend operator fun invoke(
        artistId: String
    ): Result<ArtistDetail> = runCatching {
        artistsRepository.getArtistDetail(artistId)
    }
}