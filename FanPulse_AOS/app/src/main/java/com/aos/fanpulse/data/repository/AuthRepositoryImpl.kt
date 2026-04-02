package com.aos.fanpulse.data.repository

import androidx.datastore.core.DataStore
import com.aos.fanpulse.datastore.UserData
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 데이터의 입구와 출구 역할
 * */
class AuthRepositoryImpl @Inject constructor(
    private val userDataStore: DataStore<UserData>, // DataStore를 직접 주입받아야 합니다.
) : AuthenticationRepository {

    // 1. Access Token 읽기 (Flow)
    override val accessToken: Flow<String?> = userDataStore.data
        .map { it.accessToken.ifEmpty { null } }

    // 2. Refresh Token 읽기 (Flow)
    override val refreshToken: Flow<String?> = userDataStore.data
        .map { it.refreshToken.ifEmpty { null } }

    override suspend fun updateTokens(access: String, refresh: String) {
        userDataStore.updateData { currentData ->
            currentData.toBuilder()
                .setAccessToken(access)
                .setRefreshToken(refresh)
                .build()
        }
    }

    override suspend fun clearAll() {
        userDataStore.updateData { currentData ->
            currentData.toBuilder()
                .clearAccessToken()
                .clearRefreshToken()
                // 만약 유저 정보(id, nickname 등)가 있다면 함께 clear 하세요.
                .build()
        }
    }
}