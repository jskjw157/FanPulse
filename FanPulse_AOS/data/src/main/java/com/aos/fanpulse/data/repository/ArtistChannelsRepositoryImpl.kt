package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.remote.apiservice.ArtistChannelsApiService
import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.data.mapper.toData
import com.aos.fanpulse.domain.model.ArtistChannel
import com.aos.fanpulse.domain.model.ArtistChannelListResponse
import com.aos.fanpulse.domain.model.ArtistChannelRequest
import com.aos.fanpulse.domain.model.ChannelDiscoverResponse
import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import javax.inject.Inject

class ArtistChannelsRepositoryImpl @Inject constructor(
    private val apiService: ArtistChannelsApiService
) : ArtistChannelsRepository {

    /**
     * 아티스트 채널 목록 조회
     */
    override suspend fun getArtistChannels(): ArtistChannelListResponse {
        val response = apiService.getArtistChannels()
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Empty Response")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 새로운 아티스트 채널 등록
     * (Domain Request -> Data Request 변환 필요)
     */
    override suspend fun createArtistChannel(
        request: ArtistChannelRequest
    ): ArtistChannel {
        // 도메인 모델 request를 서버용 DTO request로 변환(.toData())
        val response = apiService.createArtistChannel(request.toData())

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Create Failed")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 새로운 채널 발견 및 일괄 등록
     */
    override suspend fun discoverChannels(): ChannelDiscoverResponse {
        val response = apiService.discoverChannels()
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Discover Failed")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 특정 ID로 채널 상세 정보 조회
     */
    override suspend fun getArtistChannelDetail(channelId: String): ArtistChannel {
        val response = apiService.getArtistChannelDetail(channelId)
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Not Found")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 아티스트 채널 삭제 (Unit 반환)
     */
    override suspend fun deleteArtistChannel(id: String) {
        val response = apiService.deleteArtistChannel(id)
        if (!response.isSuccessful) {
            throw Exception("Delete Failed: ${response.code()}")
        }
    }

    /**
     * 아티스트 채널 정보 부분 수정 (PATCH)
     */
    override suspend fun patchArtistChannel(id: String) {
        val response = apiService.patchArtistChannel(id)
        if (!response.isSuccessful) {
            throw Exception("Update Failed: ${response.code()}")
        }
    }

    /**
     * 특정 아티스트의 채널 목록 조회
     */
    override suspend fun getArtistChannelsByArtistId(artistId: String): ArtistChannelListResponse {
        val response = apiService.getArtistChannels(artistId)
        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Empty List")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }
}