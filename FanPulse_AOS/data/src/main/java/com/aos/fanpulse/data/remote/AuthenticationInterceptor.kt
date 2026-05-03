package com.aos.fanpulse.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import dagger.Lazy

class AuthenticationInterceptor @Inject constructor(
    private val tokenCache: Lazy<TokenCache>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenCache.get().accessToken

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Cookie", "fanpulse_access_token=$token")
        }

        return chain.proceed(requestBuilder.build())
    }
}