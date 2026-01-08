package com.aos.fanpulse.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoogleAuthModule {
    // Google ID 옵션 제공 함수
    // setFilterByAuthorizedAccounts 매개변수를 true로 설정하여
    // API를 호출하여 사용자가 이전에 앱에 로그인하는 데 사용한 계정이 있는지 확인
    @Provides
    @Singleton
    fun provideGoogleIdOptions(): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false) // 재방문 사용자의 자동 로그인 사용 설정
            .build()
    }

    // Google의 최신 인증 API를 사용하는 객체
    @Provides
    @Singleton
    fun provideCredentialManager(
        @ApplicationContext context: Context
    ): CredentialManager {
        return CredentialManager.create(context)
    }
}