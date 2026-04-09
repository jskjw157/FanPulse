package com.aos.fanpulse.di

import com.aos.fanpulse.data.remote.apiservice.ArtistChannelsApiService
import com.aos.fanpulse.data.remote.apiservice.ArtistsApiService
import com.aos.fanpulse.data.remote.AuthenticationInterceptor
import com.aos.fanpulse.data.remote.apiservice.AuthenticationApiService
import com.aos.fanpulse.data.remote.apiservice.ChartsApiService
import com.aos.fanpulse.data.remote.apiservice.CommentsApiService
import com.aos.fanpulse.data.remote.apiservice.NewsApiService
import com.aos.fanpulse.data.remote.apiservice.SearchApiService
import com.aos.fanpulse.data.remote.apiservice.StreamingEventsApiService
import com.aos.fanpulse.data.remote.TokenAuthenticator
import com.aos.fanpulse.data.remote.apiservice.UserProfileApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://192.168.201.57:8080/api/v1/"

    // 브라우저의 쿠키 보관함 역할을 합니다.
    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar {
        return JavaNetCookieJar(CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        })
    }

    //  OkHttpClient 설정 (인터셉터 주입)
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieJar: CookieJar,
        tokenAuthenticator: TokenAuthenticator,
        authInterceptor: AuthenticationInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)                                               // withCredentials: true 역할을 수행
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)                                  // 401 에러(자동 갱신) 처리
            .addInterceptor(HttpLoggingInterceptor().apply {        // 통신 로그캣 출력
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())     // JSON 변환기
            .build()
    }

    //      ApiService     //
    //  키오스크
    @Provides
    @Singleton
    fun provideAuthenticationApiService(retrofit: Retrofit): AuthenticationApiService {
        return retrofit.create(AuthenticationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideArtistsApiService(retrofit: Retrofit): ArtistsApiService {
        return retrofit.create(ArtistsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideArtistChannelsApiService(retrofit: Retrofit): ArtistChannelsApiService {
        return retrofit.create(ArtistChannelsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChartsApiService(retrofit: Retrofit): ChartsApiService {
        return retrofit.create(ChartsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentsApiService(retrofit: Retrofit): CommentsApiService {
        return retrofit.create(CommentsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSearchApiService(retrofit: Retrofit): SearchApiService {
        return retrofit.create(SearchApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideStreamingEventsApiService(retrofit: Retrofit): StreamingEventsApiService {
        return retrofit.create(StreamingEventsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserProfileApiService(retrofit: Retrofit): UserProfileApiService {
        return retrofit.create(UserProfileApiService::class.java)
    }
}