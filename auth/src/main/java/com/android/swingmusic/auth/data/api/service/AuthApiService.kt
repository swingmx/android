package com.android.swingmusic.auth.data.api.service

import com.android.swingmusic.auth.data.dto.AllUsersDto
import com.android.swingmusic.auth.data.dto.LogInResultDto
import com.android.swingmusic.auth.data.dto.UserDto
import com.android.swingmusic.auth.domain.model.CreateUserRequest
import com.android.swingmusic.auth.domain.model.LogInRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface AuthApiService {

    @GET
    suspend fun getAllUsers(
        @Url url: String // from user input or database -> BASE_URL/auth/users
    ): AllUsersDto

    @GET
    suspend fun logInWithQrCode(
        @Url url: String, // from the Qr Code -> BASE_URL/auth/pair
        @Query("code") pairCode: String,
    ): LogInResultDto

    @POST
    suspend fun logInWithUsernameAndPassword(
        @Url url: String,
        @Body logInRequest: LogInRequest,
    ): LogInResultDto

    @POST
    suspend fun createUser(
        @Url baseUrl: String,
        @Header("Authorization") bearerAccessToken: String,
        @Body createUserRequest: CreateUserRequest
    ): UserDto
}
