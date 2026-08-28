package com.aos.fanpulse.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import dagger.Lazy

class AuthenticationInterceptor @Inject constructor(
    private val tokenCache: Lazy<TokenCache>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.header("Cookie") != null) {
            return chain.proceed(request)
        }

        val token = tokenCache.get().accessToken
        val newRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .addHeader("Cookie", "fanpulse_access_token=$token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}