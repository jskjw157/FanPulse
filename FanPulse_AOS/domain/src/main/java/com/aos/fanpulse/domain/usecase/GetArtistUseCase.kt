package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ArtistListResponse
import com.aos.fanpulse.domain.model.BaseResponse
import com.aos.fanpulse.domain.repository.ArtistsRepository
import javax.inject.Inject

class GetArtistUseCase @Inject constructor(
    private val artistsRepository: ArtistsRepository
) {
    suspend operator fun invoke(
        activeOnly: Boolean = true,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc"
    ): Result<BaseResponse<ArtistListResponse>> = runCatching {

        artistsRepository.getArtists(
            activeOnly = activeOnly,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )
    }
}