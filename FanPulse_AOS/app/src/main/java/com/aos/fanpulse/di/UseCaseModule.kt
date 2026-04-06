package com.aos.fanpulse.di

import com.aos.fanpulse.data.remote.apiservice.AuthenticationApiService
import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import com.aos.fanpulse.domain.repository.ArtistsRepository
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import com.aos.fanpulse.domain.repository.ChartsRepository
import com.aos.fanpulse.domain.repository.CommentsRepository
import com.aos.fanpulse.domain.repository.NewsRepository
import com.aos.fanpulse.domain.repository.SearchRepository
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import com.aos.fanpulse.domain.repository.UserProfileRepository
import com.aos.fanpulse.domain.usecase.ChangePasswordUseCase
import com.aos.fanpulse.domain.usecase.CreateCommentUseCase
import com.aos.fanpulse.domain.usecase.DiscoverAndSyncChannelsUseCase
import com.aos.fanpulse.domain.usecase.GetChartByDateUseCase
import com.aos.fanpulse.domain.usecase.GetChartHistoryUseCase
import com.aos.fanpulse.domain.usecase.GetCommentsUseCase
import com.aos.fanpulse.domain.usecase.GetNewsDetailUseCase
import com.aos.fanpulse.domain.usecase.GetNewsListUseCase
import com.aos.fanpulse.domain.usecase.GetStreamingEventDetailUseCase
import com.aos.fanpulse.domain.usecase.GetStreamingEventListUseCase
import com.aos.fanpulse.domain.usecase.GoogleSignInUseCase
import com.aos.fanpulse.domain.usecase.SearchAllUseCase
import com.aos.fanpulse.domain.usecase.SearchArtistsUseCase
import com.aos.fanpulse.domain.usecase.SearchNewsUseCase
import com.aos.fanpulse.domain.usecase.UpdateArtistChannelUseCase
import com.aos.fanpulse.domain.usecase.UpdateProfileUseCase
import com.aos.fanpulse.domain.usecase.UpdateSettingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // GoogleLoginUseCase.kt
    @Provides
    @Singleton
    fun provideGoogleLoginUseCase(
        googleSignInRepository: GoogleSignInRepository,
        authApiService: AuthenticationApiService,
        authRepository: AuthenticationRepository
    ): GoogleSignInUseCase = GoogleSignInUseCase(googleSignInRepository, authApiService, authRepository)

    //  ArtistsRepository
    //  SearchArtistsUseCase.kt
    @Provides
    @Singleton
    fun provideSearchArtistsUseCase(
        artistsRepository: ArtistsRepository
    ): SearchArtistsUseCase = SearchArtistsUseCase(artistsRepository)

    //  ArtistChannelsRepository
    //  UpdateArtistChannelUseCase.kt
    @Provides
    @Singleton
    fun provideUpdateArtistChannelUseCase(
        artistChannelsRepository: ArtistChannelsRepository
    ): UpdateArtistChannelUseCase = UpdateArtistChannelUseCase(artistChannelsRepository)

    //  DiscoverAndSyncChannelsUseCase.kt
    @Provides
    @Singleton
    fun provideDiscoverAndSyncChannelsUseCase(
        artistChannelsRepository: ArtistChannelsRepository
    ): DiscoverAndSyncChannelsUseCase = DiscoverAndSyncChannelsUseCase(artistChannelsRepository)

    //  ChartsRepository
    //  GetChartHistoryUseCase.kt
    @Provides
    @Singleton
    fun provideGetChartHistoryUseCase(
        chartsRepository: ChartsRepository
    ): GetChartHistoryUseCase = GetChartHistoryUseCase(chartsRepository)

    //  GetChartByDateUseCase.kt
    @Provides
    @Singleton
    fun provideGetChartByDateUseCase(
        chartsRepository: ChartsRepository
    ): GetChartByDateUseCase = GetChartByDateUseCase(chartsRepository)

    //  CommentsRepository
    //  GetCommentsUseCase.kt
    @Provides
    @Singleton
    fun provideGetCommentsUseCase(
        commentsRepository: CommentsRepository
    ): GetCommentsUseCase = GetCommentsUseCase(commentsRepository)

    //  CreateCommentUseCase.kt
    @Provides
    @Singleton
    fun provideCreateCommentUseCase(
        commentsRepository: CommentsRepository
    ): CreateCommentUseCase = CreateCommentUseCase(commentsRepository)

    //  NewsRepository
    //  SearchNewsUseCase.kt
    @Provides
    @Singleton
    fun provideSearchNewsUseCase(
        newsRepository: NewsRepository
    ): SearchNewsUseCase = SearchNewsUseCase(newsRepository)

    //  GetNewsDetailUseCase.kt
    @Provides
    @Singleton
    fun provideGetNewsDetailUseCase(
        newsRepository: NewsRepository
    ): GetNewsDetailUseCase = GetNewsDetailUseCase(newsRepository)

    //  GetNewsListUseCase.kt
    @Provides
    @Singleton
    fun provideGetNewsListUseCase(
        newsRepository: NewsRepository
    ): GetNewsListUseCase = GetNewsListUseCase(newsRepository)

    //  SearchRepository
    //  SearchAllUseCase.kt
    @Provides
    @Singleton
    fun provideSearchAllUseCase(
        searchRepository: SearchRepository
    ): SearchAllUseCase = SearchAllUseCase(searchRepository)

    //  StreamingEventsRepository
    //  GetStreamingEventListUseCase.kt
    @Provides
    @Singleton
    fun provideGetStreamingEventListUseCase(
        streamingEventsRepository: StreamingEventsRepository
    ): GetStreamingEventListUseCase = GetStreamingEventListUseCase(streamingEventsRepository)

    //  GetStreamingEventDetailUseCase.kt
    @Provides
    @Singleton
    fun provideGetStreamingEventDetailUseCase(
        streamingEventsRepository: StreamingEventsRepository
    ): GetStreamingEventDetailUseCase = GetStreamingEventDetailUseCase(streamingEventsRepository)

    //  UserProfileRepository
    //  ChangePasswordUseCase.kt
    @Provides
    @Singleton
    fun provideChangePasswordUseCase(
        userProfileRepository: UserProfileRepository
    ): ChangePasswordUseCase = ChangePasswordUseCase(userProfileRepository)

    //  UpdateProfileUseCase.kt
    @Provides
    @Singleton
    fun provideUpdateProfileUseCase(
        userProfileRepository: UserProfileRepository
    ): UpdateProfileUseCase = UpdateProfileUseCase(userProfileRepository)

    //  UpdateSettingsUseCase.kt
    @Provides
    @Singleton
    fun provideUpdateSettingsUseCase(
        userProfileRepository: UserProfileRepository
    ): UpdateSettingsUseCase = UpdateSettingsUseCase(userProfileRepository)
}