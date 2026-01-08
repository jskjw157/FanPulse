package com.aos.fanpulse.di

import com.aos.fanpulse.domain.repository.GoogleSignInRepository
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
        googleSignInRepository: GoogleSignInRepository
    ): GoogleLoginUseCase = GoogleLoginUseCase(googleSignInRepository)
}