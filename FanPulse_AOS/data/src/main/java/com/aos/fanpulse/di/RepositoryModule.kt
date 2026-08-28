package com.aos.fanpulse.di

import com.aos.fanpulse.data.repository.ArtistChannelsRepositoryImpl
import com.aos.fanpulse.data.repository.ArtistsRepositoryImpl
import com.aos.fanpulse.data.repository.AuthRepositoryImpl
import com.aos.fanpulse.data.repository.BookmarkRepositoryImpl
import com.aos.fanpulse.data.repository.ChartsRepositoryImpl
import com.aos.fanpulse.data.repository.CommentsRepositoryImpl
import com.aos.fanpulse.data.repository.ImageStorageRepositoryImpl
import com.aos.fanpulse.data.repository.LikeRepositoryImpl
import com.aos.fanpulse.data.repository.MusicRepositoryImpl
import com.aos.fanpulse.data.repository.NewsRepositoryImpl
import com.aos.fanpulse.data.repository.NotificationRepositoryImpl
import com.aos.fanpulse.data.repository.PostRepositoryImpl
import com.aos.fanpulse.data.repository.SearchRepositoryImpl
import com.aos.fanpulse.data.repository.SettingRepositoryImpl
import com.aos.fanpulse.data.repository.StreamingEventsRepositoryImpl
import com.aos.fanpulse.data.repository.UserProfileRepositoryImpl
import com.aos.fanpulse.domain.repository.ArtistChannelsRepository
import com.aos.fanpulse.domain.repository.ArtistsRepository
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import com.aos.fanpulse.domain.repository.BookmarkRepository
import com.aos.fanpulse.domain.repository.ChartsRepository
import com.aos.fanpulse.domain.repository.CommentsRepository
import com.aos.fanpulse.domain.repository.ImageStorageRepository
import com.aos.fanpulse.domain.repository.LikeRepository
import com.aos.fanpulse.domain.repository.MusicRepository
import com.aos.fanpulse.domain.repository.NewsRepository
import com.aos.fanpulse.domain.repository.NotificationRepository
import com.aos.fanpulse.domain.repository.PostRepository
import com.aos.fanpulse.domain.repository.SearchRepository
import com.aos.fanpulse.domain.repository.SettingRepository
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

    //  차트 레포지토리
    @Binds
    @Singleton
    abstract fun bindChartsRepository(
        chartsRepositoryImpl: ChartsRepositoryImpl
    ): ChartsRepository

    //  댓글 레포지토리
    @Binds
    @Singleton
    abstract fun bindCommentsRepository(
        commentsRepositoryImpl: CommentsRepositoryImpl
    ): CommentsRepository

    //  뉴스 레포지토리
    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository

    //  검색 레포지토리
    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    //  스트리밍 이벤트 레포지토리
    @Binds
    @Singleton
    abstract fun bindStreamingEventsRepository(
        streamingEventsRepositoryImpl: StreamingEventsRepositoryImpl
    ): StreamingEventsRepository

    //  유저 프로필 레포지토리
    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository

    //  게시물 올리기 레포지토리
    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    //  LastFm 레포지토리
    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    //  FCM
    @Binds
    @Singleton
    abstract fun bindSettingRepository(
        settingRepositoryImpl: SettingRepositoryImpl
    ): SettingRepository

    // 알림 DB
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    //  좋아요 DB
    @Binds
    @Singleton
    abstract fun bindLikeRepository(
        likeRepositoryImpl: LikeRepositoryImpl
    ): LikeRepository

    //  북마크 DB
    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl
    ): BookmarkRepository

    //  Storage
    @Binds
    @Singleton
    abstract fun bindImageStorageRepository(
        imageStorageRepositoryImpl: ImageStorageRepositoryImpl
    ): ImageStorageRepository
}