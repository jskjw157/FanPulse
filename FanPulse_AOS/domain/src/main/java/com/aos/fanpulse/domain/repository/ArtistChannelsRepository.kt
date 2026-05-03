package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.ArtistChannel
import com.aos.fanpulse.domain.model.ArtistChannelListResponse
import com.aos.fanpulse.domain.model.ArtistChannelRequest
import com.aos.fanpulse.domain.model.ChannelDiscoverResponse

interface ArtistChannelsRepository {
    // 아티스트 채널 목록 조회 (Admin 전용)
    suspend fun getArtistChannels(): ArtistChannelListResponse

    // 새로운 아티스트 채널 등록
    suspend fun createArtistChannel(request: ArtistChannelRequest): ArtistChannel

    // 새로운 채널 발견 및 일괄 등록/업데이트 수행
    suspend fun discoverChannels(): ChannelDiscoverResponse

    // 특정 ID로 채널 상세 정보 조회
    suspend fun getArtistChannelDetail(channelId: String): ArtistChannel

    // 아티스트 채널 삭제
    suspend fun deleteArtistChannel(id: String)

    // 아티스트 채널 정보 부분 수정 (PATCH)
    suspend fun patchArtistChannel(id: String)

    // 특정 아티스트의 채널 목록 조회
    suspend fun getArtistChannelsByArtistId(artistId: String): ArtistChannelListResponse
}