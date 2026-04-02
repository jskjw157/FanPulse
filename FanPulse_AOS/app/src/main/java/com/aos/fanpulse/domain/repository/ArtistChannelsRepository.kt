package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ArtistChannel
import com.aos.fanpulse.data.remote.apiservice.ArtistChannelListResponse
import com.aos.fanpulse.data.remote.apiservice.ArtistChannelRequest
import com.aos.fanpulse.data.remote.apiservice.ArtistChannelsApiService
import com.aos.fanpulse.data.remote.apiservice.ChannelDiscoverResponse
import retrofit2.Response
import javax.inject.Inject

class ArtistChannelsRepository @Inject constructor(
    private val apiService: ArtistChannelsApiService
) {
    /**
     * 아티스트 채널 목록 조회 (Admin 전용)
     */
    suspend fun getArtistChannels(): Response<ArtistChannelListResponse> {
        return apiService.getArtistChannels()
    }

    /**
     * 새로운 아티스트 채널 등록
     */
    suspend fun createArtistChannel(
        request: ArtistChannelRequest
    ): Response<ArtistChannel> {
        return apiService.createArtistChannel(request)
    }

    /**
     * 새로운 채널 발견 및 일괄 등록/업데이트 수행
     */
    suspend fun discoverChannels(): Response<ChannelDiscoverResponse> {
        return apiService.discoverChannels()
    }

    /**
     * 특정 ID로 채널 상세 정보 조회
     */
    suspend fun getArtistChannelDetail(
        channelId: String
    ): Response<ArtistChannel> {
        return apiService.getArtistChannelDetail(channelId)
    }

    /**
     * 아티스트 채널 삭제
     */
    suspend fun deleteArtistChannel(
        id: String
    ): Response<Unit> {
        return apiService.deleteArtistChannel(id)
    }

    /**
     * 아티스트 채널 정보 부분 수정 (PATCH)
     * (주의: ApiService에 누락되었던 수정할 데이터 파라미터를 추가했습니다)
     */
    suspend fun patchArtistChannel(
        id: String,
        // request: PatchArtistChannelRequest // 실제 사용하는 Request 클래스로 변경하세요
    ): Response<Unit> {
        return apiService.patchArtistChannel(id/*, request*/)
    }

    /**
     * 특정 아티스트의 채널 목록 조회
     */
    suspend fun getArtistChannels(
        artistId: String
    ): Response<ArtistChannelListResponse> {
        return apiService.getArtistChannels(artistId)
    }
}