package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ArtistChannel
import com.aos.fanpulse.data.remote.apiservice.ArtistChannelListResponse
import com.aos.fanpulse.data.remote.apiservice.ArtistChannelRequest
import com.aos.fanpulse.data.remote.apiservice.ChannelDiscoverResponse
import retrofit2.Response

interface ArtistChannelsRepository {
    // 아티스트 채널 목록 조회 (Admin 전용)
    suspend fun getArtistChannels(): Response<ArtistChannelListResponse>

    // 새로운 아티스트 채널 등록
    suspend fun createArtistChannel(request: ArtistChannelRequest): Response<ArtistChannel>

    // 새로운 채널 발견 및 일괄 등록/업데이트 수행
    suspend fun discoverChannels(): Response<ChannelDiscoverResponse>

    // 특정 ID로 채널 상세 정보 조회
    suspend fun getArtistChannelDetail(channelId: String): Response<ArtistChannel>

    // 아티스트 채널 삭제
    suspend fun deleteArtistChannel(id: String): Response<Unit>

    // 아티스트 채널 정보 부분 수정 (PATCH)
    suspend fun patchArtistChannel(id: String): Response<Unit>

    // 특정 아티스트의 채널 목록 조회
    suspend fun getArtistChannelsByArtistId(artistId: String): Response<ArtistChannelListResponse>
}