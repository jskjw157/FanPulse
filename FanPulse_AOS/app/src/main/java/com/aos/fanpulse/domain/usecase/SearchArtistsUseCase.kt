package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.ArtistsRepository
import javax.inject.Inject

class SearchArtistsUseCase@Inject constructor(
    private val artistsRepository: ArtistsRepository
){
    suspend operator fun invoke(
        query: String,
        page: Int = 0,
        size: Int = 20
    ) = artistsRepository.searchArtists(query, page, size)
}