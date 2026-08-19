package com.aos.fanpulse.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==========================================
// 공통으로 재사용되는 DTO
// ==========================================

data class LastFmArtist(
    @SerializedName("name") val name: String,
    @SerializedName("mbid") val mbid: String,
    @SerializedName("url") val url: String,
    @SerializedName("image") val images: List<LastFmImage>?
)

data class LastFmImage(
    @SerializedName("#text") val imageUrl: String,
    @SerializedName("size") val size: String
)

// ==========================================
// 아티스트 검색 (Artist Search) 전용 DTO
// ==========================================

data class ArtistSearchResponse(
    @SerializedName("results") val results: ArtistResults
)

data class ArtistResults(
    @SerializedName("artistmatches") val artistMatches: ArtistMatches
)

data class ArtistMatches(
    @SerializedName("artist") val artistList: List<LastFmArtist>
)

// ==========================================
// 인기 트랙 (Top Tracks) 전용 DTO
// ==========================================

// 최상위 응답 객체
data class LastFmTopTracksResponse(
    @SerializedName("tracks") val tracks: LastFmTopTracks
)

// 트랙 리스트와 페이징(메타데이터) 정보를 담은 객체
data class LastFmTopTracks(
    @SerializedName("track") val trackList: List<LastFmTrack>,
    @SerializedName("@attr") val attr: LastFmChartAttr?
)

// 개별 트랙(곡) 정보
data class LastFmTrack(
    @SerializedName("name") val name: String,
    @SerializedName("playcount") val playcount: String?,
    @SerializedName("listeners") val listeners: String?,
    @SerializedName("url") val url: String,
    @SerializedName("artist") val artist: LastFmArtist,
    @SerializedName("image") val images: List<LastFmImage>?
)

// 차트 페이징 속성 (현재 페이지, 전체 페이지 수 등)
data class LastFmChartAttr(
    @SerializedName("page") val page: String,
    @SerializedName("perPage") val perPage: String,
    @SerializedName("totalPages") val totalPages: String,
    @SerializedName("total") val total: String
)