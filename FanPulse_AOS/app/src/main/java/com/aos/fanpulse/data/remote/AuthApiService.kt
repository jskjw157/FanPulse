package com.aos.fanpulse.data.remote

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<Unit>

    @POST("auth/refresh")
    fun refreshTokens(string: String): Call<Unit>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}

data class GoogleLoginRequest(val idToken: String)