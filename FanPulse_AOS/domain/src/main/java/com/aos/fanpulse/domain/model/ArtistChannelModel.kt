package com.aos.fanpulse.domain.model

// 1. 아티스트 채널 목록 응답 (페이징)
data class ArtistChannelListResponse(
    val content: List<ArtistChannel>,
    val totalElements: Int
)

// 2. 아티스트 채널 상세 정보
data class ArtistChannel(
    val id: String,
    val artistId: String,
    val platform: String,           // 예: "YOUTUBE"
    val channelHandle: String?,      // 예: "@IVEstarship"
    val channelId: String?,
    val channelUrl: String?,
    val isOfficial: Boolean,
    val isActive: Boolean,
    val lastCrawledAt: String?,
    val createdAt: String
)

// 3. 채널 등록/수정 요청 바디
data class ArtistChannelRequest(
    val artistId: String,
    val platform: String,
    val channelHandle: String,
    val channelId: String?,
    val channelUrl: String?,
    val isOfficial: Boolean = true,
    val isActive: Boolean = true
)

// 4. 채널 발견(Discover) 결과 응답
data class ChannelDiscoverResponse(
    val total: Int,
    val upserted: Int,
    val failed: Int,
    val errors: List<String>
)
