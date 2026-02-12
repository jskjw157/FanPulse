package com.aos.fanpulse.data.datasource

import androidx.datastore.core.DataStore
import com.aos.fanpulse.datastore.UserData
import com.aos.fanpulse.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 데이터의 입구와 출구 역할
 * */
class UserDataRepositoryImpl @Inject constructor(
    private val userDataStore: DataStore<UserData>
) : UserDataRepository {

    override val idToken: Flow<String?> = userDataStore.data
        .map { it.idToken.ifEmpty { null } }

    override suspend fun updateIdToken(newToken: String) {
        userDataStore.updateData { it.toBuilder().setIdToken(newToken).build() }
    }

    override suspend fun clearIdToken() {
        userDataStore.updateData { it.toBuilder().clearIdToken().build() }
    }

}