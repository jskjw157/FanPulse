package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.ArtistSearchResponse
import com.aos.fanpulse.data.remote.dto.LastFmTopTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFmService {
    @GET("?method=artist.search")
    suspend fun searchArtist(
        @Query("artist") artistName: String
    ): ArtistSearchResponse

    @GET("?method=chart.gettoptracks")
    suspend fun getTopTracks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): LastFmTopTracksResponse

    @GET("?method=geo.gettoptracks")
    suspend fun getKoreaTopTracks(
        @Query("country") country: String = "Korea, Republic of",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): LastFmTopTracksResponse
}