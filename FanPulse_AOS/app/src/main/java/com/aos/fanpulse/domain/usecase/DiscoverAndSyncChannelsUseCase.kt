package com.aos.fanpulse.domain.usecase

import android.util.Log
import com.aos.fanpulse.data.remote.apiservice.ChannelDiscoverResponse
import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import retrofit2.Response
import javax.inject.Inject

//  ArtistChannelsRepository - discoverChannels()
class DiscoverAndSyncChannelsUseCase@Inject constructor(
    private val repository: ArtistChannelsRepository
) {
    suspend operator fun invoke(): Response<ChannelDiscoverResponse> {

        // 1. 서버에 새로운 채널 발견(Discover) 요청
        Log.d("DiscoverUseCase", "서버에 새로운 채널 탐색을 요청합니다...")
        val response = repository.discoverChannels()

        // 2. 통신이 성공적일 때 동기화(Sync) 로직 실행
        if (response.isSuccessful) {
            val discoverData = response.body()

            if (discoverData != null) {
//                Log.d("DiscoverUseCase", "탐색 성공! ${discoverData.newChannelsCount}개의 새 채널 발견.")

                // [여기에 동기화 로직이 들어갑니다]
                // 예: 서버에서 찾은 새 채널 목록을 안드로이드 기기(Room DB)에 저장
                // channelDao.insertChannels(discoverData.channels)

                // 필요하다면 데이터를 정렬하거나 변환하는 작업도 여기서 수행합니다.
            }
        } else {
            Log.e("DiscoverUseCase", "채널 탐색 실패: HTTP ${response.code()}")
        }
        return response
    }
}