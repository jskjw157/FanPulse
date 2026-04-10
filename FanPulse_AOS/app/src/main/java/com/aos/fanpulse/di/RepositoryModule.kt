package com.aos.fanpulse.di

import com.aos.fanpulse.data.repository.AuthRepositoryImpl
import com.aos.fanpulse.data.repository.GoogleSignInRepositoryImpl
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // GoogleSignInRepository 추가
    @Binds
    @Singleton
    abstract fun bindsGoogleSignInRepository(
        googleSignInRepositoryImpl: GoogleSignInRepositoryImpl
    ): GoogleSignInRepository

    // "AuthRepository(인터페이스)를 요청하면, AuthRepositoryImpl(진짜 구현체)을 줘라!" 라고 Hilt에게 규칙을 알려줍니다.
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthenticationRepository
}