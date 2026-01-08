package com.aos.fanpulse.di

import com.aos.fanpulse.data.repository.GoogleSignInRepositoryImpl
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
}