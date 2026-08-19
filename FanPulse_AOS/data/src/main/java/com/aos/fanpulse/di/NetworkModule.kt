package com.aos.fanpulse.di

import com.aos.fanpulse.LastFmNetwork
import com.aos.fanpulse.MainNetwork
import com.aos.fanpulse.data.BuildConfig
import com.aos.fanpulse.data.BuildConfig.BASE_URL
import com.aos.fanpulse.data.BuildConfig.LAST_FM_URL
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
import com.aos.fanpulse.data.remote.apiservice.LastFmService
import com.aos.fanpulse.data.remote.apiservice.UserProfileApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.Interceptor
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

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar {
        return JavaNetCookieJar(CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        })
    }

    @Provides
    @Singleton
    @MainNetwork
    fun provideOkHttpClient(
        cookieJar: CookieJar,
        tokenAuthenticator: TokenAuthenticator,
        authInterceptor: AuthenticationInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @MainNetwork
    fun provideRetrofit(
        @MainNetwork okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    /**
     * LastFmNetwork
     * */
    @Provides
    @Singleton
    @LastFmNetwork
    fun provideLastFmOkHttpClient(): OkHttpClient {
        val lastFmAuthInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newUrl = originalRequest.url.newBuilder()
                .addQueryParameter("api_key", BuildConfig.LASTFM_API_KEY)
                .addQueryParameter("format", "json")
                .build()

            chain.proceed(originalRequest.newBuilder().url(newUrl).build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(lastFmAuthInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @LastFmNetwork // 한정자 추가
    fun provideLastFmRetrofit(
        @LastFmNetwork okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(LAST_FM_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     *      ApiService
     * */

    @Provides
    @Singleton
    fun provideLastFmService(
        @LastFmNetwork retrofit: Retrofit
    ): LastFmService{
        return retrofit.create(LastFmService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthenticationApiService(@MainNetwork retrofit: Retrofit): AuthenticationApiService {
        return retrofit.create(AuthenticationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideArtistsApiService(@MainNetwork retrofit: Retrofit): ArtistsApiService {
        return retrofit.create(ArtistsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideArtistChannelsApiService(@MainNetwork retrofit: Retrofit): ArtistChannelsApiService {
        return retrofit.create(ArtistChannelsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChartsApiService(@MainNetwork retrofit: Retrofit): ChartsApiService {
        return retrofit.create(ChartsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentsApiService(@MainNetwork retrofit: Retrofit): CommentsApiService {
        return retrofit.create(CommentsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsApiService(@MainNetwork retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSearchApiService(@MainNetwork retrofit: Retrofit): SearchApiService {
        return retrofit.create(SearchApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideStreamingEventsApiService(@MainNetwork retrofit: Retrofit): StreamingEventsApiService {
        return retrofit.create(StreamingEventsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserProfileApiService(@MainNetwork retrofit: Retrofit): UserProfileApiService {
        return retrofit.create(UserProfileApiService::class.java)
    }
}