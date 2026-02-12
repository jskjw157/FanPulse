package com.aos.fanpulse.data.remote

import com.aos.fanpulse.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userDataRepository: UserDataRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            userDataRepository.idToken.first()
        }

        val originalRequest = chain.request()

        // 2. 토큰이 있을 경우에만 헤더에 추가합니다.
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}