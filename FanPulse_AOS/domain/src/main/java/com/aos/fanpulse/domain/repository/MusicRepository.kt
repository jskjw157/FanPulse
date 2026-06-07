package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.ChartTrack
import com.aos.fanpulse.domain.model.LastFmArtist

interface MusicRepository {
    suspend fun searchArtist(name: String): Result<List<LastFmArtist>>

    suspend fun getTopTracks(page: Int, limit: Int): Result<List<ChartTrack>>

    suspend fun getKoreaTopTracks(page: Int, limit: Int): Result<List<ChartTrack>>
}