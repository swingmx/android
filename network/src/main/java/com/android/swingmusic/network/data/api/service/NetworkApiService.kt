package com.android.swingmusic.network.data.api.service

import com.android.swingmusic.core.data.dto.AllArtistsDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.network.data.dto.AddFavoriteRequest
import com.android.swingmusic.network.data.dto.LogTrackRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface NetworkApiService {
    @POST
    suspend fun getFoldersAndTracks(
        @Body requestData: FoldersAndTracksRequestDto,
        @Url url: String,
        @Header("Authorization") bearerToken: String,
    ): FoldersAndTracksDto

    @POST
    suspend fun logLastPlayedTrackToServer(
        @Body logTrackRequest: LogTrackRequestDto,
        @Url url: String,
        @Header("Authorization") bearerToken: String,
    ): Any

    @GET
    suspend fun getAllArtists(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") pageSize: Int = 20,
        @Query("start") startIndex: Int = 0,
        @Query("sortby") sortBy: String,
        @Query("reverse") sortOrder: Int
    ): AllArtistsDto

    @GET
    suspend fun getSampleArtist(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") pageSize: Int = 1,
    ): AllArtistsDto

    @POST
    suspend fun addFavorite(
        @Url url: String,
        @Body addFavoriteRequest: AddFavoriteRequest,
        @Header("Authorization") bearerToken: String,
    ): Any

    @POST
    suspend fun removeFavorite(
        @Url url: String,
        @Body addFavoriteRequest: AddFavoriteRequest,
        @Header("Authorization") bearerToken: String,
    ): Any
}
