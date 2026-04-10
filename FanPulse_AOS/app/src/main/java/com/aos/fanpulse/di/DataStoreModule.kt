package com.aos.fanpulse.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.aos.fanpulse.ApplicationScope
import com.aos.fanpulse.data.local.UserDataSerializer
import com.aos.fanpulse.datastore.UserData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope
    ): DataStore<UserData> {
        return DataStoreFactory.create(
            serializer = UserDataSerializer,
            produceFile = { context.dataStoreFile("user_data.pb") }, // 파일명 설정
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { UserData.getDefaultInstance() } // 파일 손상 시 빈 데이터(초기값)로 대체
            ),
            migrations = emptyList(),
            scope = scope
        )
    }
}