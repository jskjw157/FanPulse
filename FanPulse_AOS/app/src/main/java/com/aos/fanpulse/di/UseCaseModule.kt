package com.aos.fanpulse.di

import com.aos.fanpulse.data.remote.AuthApiService
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import com.aos.fanpulse.domain.repository.AuthRepository
import com.aos.fanpulse.domain.usecase.GoogleLoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // GoogleLoginUseCase 추가
    @Provides
    @Singleton
    fun provideGoogleLoginUseCase(
        googleSignInRepository: GoogleSignInRepository,
        authApiService: AuthApiService,
        authRepository: AuthRepository
    ): GoogleLoginUseCase = GoogleLoginUseCase(googleSignInRepository, authApiService, authRepository)
}