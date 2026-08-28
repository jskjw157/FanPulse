package com.aos.fanpulse.data.mapper


import com.aos.fanpulse.data.remote.dto.ArtistSearchResponse
import com.aos.fanpulse.data.remote.dto.LastFmArtist as DataArtist
import com.aos.fanpulse.data.remote.dto.LastFmTopTracksResponse
import com.aos.fanpulse.data.remote.dto.LastFmTrack

import com.aos.fanpulse.domain.model.LastFmArtist as DomainArtist
import com.aos.fanpulse.domain.model.ChartTrack

internal fun DataArtist.toDomain(): DomainArtist {
    val targetImageUrl = this.images?.find { it.size == "large" }?.imageUrl ?: ""

    return DomainArtist(
        name = this.name,
        mbid = this.mbid,
        url = this.url,
        imageUrl = targetImageUrl
    )
}

internal fun ArtistSearchResponse.toDomainList(): List<DomainArtist> {
    val dtos = this.results.artistMatches.artistList
    return dtos.map { it.toDomain() }
}

internal fun LastFmTrack.toDomain(): ChartTrack {
    val targetImageUrl = this.images?.find { it.size == "extralarge" }?.imageUrl
        ?: this.images?.find { it.size == "large" }?.imageUrl
        ?: ""

    return ChartTrack(
        title = this.name,
        artistName = this.artist.name,
        playCount = this.playcount?.toLongOrNull() ?: 0L,
        trackUrl = this.url,
        imageUrl = targetImageUrl
    )
}

internal fun LastFmTopTracksResponse.toDomainList(): List<ChartTrack> {
    val dtos = this.tracks.trackList
    return dtos.map { it.toDomain() }
}