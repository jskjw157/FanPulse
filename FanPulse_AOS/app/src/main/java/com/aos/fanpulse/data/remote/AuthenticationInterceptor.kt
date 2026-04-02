package com.aos.fanpulse.data.remote

import com.aos.fanpulse.domain.repository.AuthenticationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthenticationInterceptor @Inject constructor(
    private val authRepository: AuthenticationRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = runBlocking {
            authRepository.accessToken.first()
        }

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Cookie", "fanpulse_access_token=$token")
        }

        return chain.proceed(requestBuilder.build())
    }
}