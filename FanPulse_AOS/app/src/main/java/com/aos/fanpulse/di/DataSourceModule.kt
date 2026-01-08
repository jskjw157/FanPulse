package com.aos.fanpulse.di

import com.aos.fanpulse.data.datasource.GoogleSignInDataSource
import com.aos.fanpulse.data.datasource.GoogleSignInDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    // GoogleSignInDataSource 추가
    @Binds
    @Singleton
    abstract fun bindGoogleSignInDataSource(
        googleSignInDataSourceImpl: GoogleSignInDataSourceImpl
    ): GoogleSignInDataSource
}