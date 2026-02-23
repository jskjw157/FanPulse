package com.aos.fanpulse.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getMyInfo(): Response<UserInfoResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}

data class GoogleLoginRequest(val idToken: String)
data class LoginResponse(val user: User)
data class UserInfoResponse(val authenticated: Boolean, val user: User?)
data class User(val id: Int, val email: String, val nickname: String)