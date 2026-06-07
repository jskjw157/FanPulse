package com.aos.fanpulse.domain.model

data class LastFmArtist(
    val name: String,
    val mbid: String,
    val url: String,
    val imageUrl: String
)

data class ChartTrack(
    val title: String,
    val artistName: String,
    val playCount: Long,
    val trackUrl: String,
    val imageUrl: String
)