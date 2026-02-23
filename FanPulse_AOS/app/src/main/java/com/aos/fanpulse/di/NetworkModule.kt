package com.aos.fanpulse.di

import com.aos.fanpulse.data.remote.AuthApiService
import com.aos.fanpulse.data.remote.AuthInterceptor
import com.aos.fanpulse.data.remote.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    val baseUrl = "http://10.0.2.2:8080/api/v1/"

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar {
        // 모든 요청에 쿠키를 자동으로 주고받게 해줍니다.
        return JavaNetCookieJar(CookieManager().apply {
            setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        })
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar) // 👈 쿠키 자동 관리 도구 장착
            // 이제 더 이상 authInterceptor를 통한 Bearer 헤더 주입은 필요 없습니다.
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}