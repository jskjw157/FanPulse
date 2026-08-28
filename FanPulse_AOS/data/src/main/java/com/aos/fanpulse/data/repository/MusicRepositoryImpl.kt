package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toDomainList
import com.aos.fanpulse.data.remote.apiservice.LastFmService
import com.aos.fanpulse.domain.model.ChartTrack
import com.aos.fanpulse.domain.model.LastFmArtist
import com.aos.fanpulse.domain.repository.MusicRepository
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val lastFmService: LastFmService
) : MusicRepository {

    override suspend fun searchArtist(name: String): Result<List<LastFmArtist>> {
        return runCatching {
            val response = lastFmService.searchArtist(name)
            response.toDomainList()
        }
    }

    override suspend fun getTopTracks(page: Int, limit: Int): Result<List<ChartTrack>> {
        return runCatching {
            val response = lastFmService.getTopTracks(page, limit)
            response.toDomainList()
        }
    }

    override suspend fun getKoreaTopTracks(page: Int, limit: Int): Result<List<ChartTrack>> {
        return runCatching {
            val response = lastFmService.getKoreaTopTracks(page = page, limit = limit)
            response.toDomainList()
        }
    }
}