package com.android.swingmusic.network.data.api.service

import com.android.swingmusic.core.data.dto.AllArtistsDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.network.domain.model.LogTrackRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface NetworkApiService {
    @POST("folder")
    suspend fun getFoldersAndTracks(
        @Body requestData: FoldersAndTracksRequestDto,
        @Header("Authorization") bearerToken: String,
    ): FoldersAndTracksDto

    @POST("logger/track/log")
    suspend fun logLastPlayedTrackToServer(
        @Body logTrackRequest: LogTrackRequest,
        @Header("Authorization") bearerToken: String,
    ): Any

    // TODO: Add Auth Header to the following methods

    @GET("getall/artists")
    suspend fun getAllArtists(
        @Query("limit") pageSize: Int = 20,
        @Query("start") startIndex: Int = 0,
        @Query("sortby") sortBy: String,
        @Query("reverse") sortOrder: Int
    ): AllArtistsDto

    @GET("getall/artists")
    suspend fun getSampleArtist(
        @Query("limit") pageSize: Int = 1,
    ): AllArtistsDto
}
