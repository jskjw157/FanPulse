package com.aos.fanpulse.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(
    private val tokenCache: TokenCache // Repository 대신 Cache를 주입받음
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenCache.accessToken

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Cookie", "fanpulse_access_token=$token")
        }

        return chain.proceed(requestBuilder.build())
    }
}