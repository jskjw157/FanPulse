package com.aos.fanpulse.di

import com.aos.fanpulse.data.repository.ArtistChannelsRepositoryImpl
import com.aos.fanpulse.data.repository.ArtistsRepositoryImpl
import com.aos.fanpulse.data.repository.AuthRepositoryImpl
import com.aos.fanpulse.data.repository.ChartsRepositoryImpl
import com.aos.fanpulse.data.repository.CommentsRepositoryImpl
import com.aos.fanpulse.data.repository.GoogleSignInRepositoryImpl
import com.aos.fanpulse.data.repository.NewsRepositoryImpl
import com.aos.fanpulse.data.repository.SearchRepositoryImpl
import com.aos.fanpulse.data.repository.StreamingEventsRepositoryImpl
import com.aos.fanpulse.data.repository.UserProfileRepositoryImpl
import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import com.aos.fanpulse.domain.repository.ArtistsRepository
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import com.aos.fanpulse.domain.repository.ChartsRepository
import com.aos.fanpulse.domain.repository.CommentsRepository
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import com.aos.fanpulse.domain.repository.NewsRepository
import com.aos.fanpulse.domain.repository.SearchRepository
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import com.aos.fanpulse.domain.repository.UserProfileRepository
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

    @Binds
    @Singleton
    abstract fun bindArtistChannelsRepository(
        artistChannelsRepositoryImpl: ArtistChannelsRepositoryImpl
    ): ArtistChannelsRepository

    @Binds
    @Singleton
    abstract fun bindArtistsRepository(
        artistsRepositoryImpl: ArtistsRepositoryImpl
    ): ArtistsRepository

    // 3. 차트 레포지토리
    @Binds
    @Singleton
    abstract fun bindChartsRepository(
        chartsRepositoryImpl: ChartsRepositoryImpl
    ): ChartsRepository

    // 4. 댓글 레포지토리
    @Binds
    @Singleton
    abstract fun bindCommentsRepository(
        commentsRepositoryImpl: CommentsRepositoryImpl
    ): CommentsRepository

    // 5. 뉴스 레포지토리
    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository

    // 6. 검색 레포지토리
    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    // 7. 스트리밍 이벤트 레포지토리
    @Binds
    @Singleton
    abstract fun bindStreamingEventsRepository(
        streamingEventsRepositoryImpl: StreamingEventsRepositoryImpl
    ): StreamingEventsRepository

    // 8. 유저 프로필 레포지토리
    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository
}