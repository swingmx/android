package com.android.swingmusic.network.data.api.service

import com.android.swingmusic.core.data.dto.AlbumWithInfoDto
import com.android.swingmusic.core.data.dto.AlbumsSearchResultDto
import com.android.swingmusic.core.data.dto.AllAlbumsDto
import com.android.swingmusic.core.data.dto.AllArtistsDto
import com.android.swingmusic.core.data.dto.ArtistDto
import com.android.swingmusic.core.data.dto.ArtistInfoDto
import com.android.swingmusic.core.data.dto.ArtistsSearchResultDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.dto.TopSearchResultsDto
import com.android.swingmusic.core.data.dto.TracksSearchResultDto
import com.android.swingmusic.network.data.dto.ToggleFavoriteRequest
import com.android.swingmusic.network.data.dto.AlbumHashRequestDto
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
    suspend fun getArtistsCount(
        // used to get the total artists value
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") pageSize: Int = 1,
    ): AllArtistsDto

    @POST
    suspend fun addFavorite(
        @Url url: String,
        @Body toggleFavoriteRequest: ToggleFavoriteRequest,
        @Header("Authorization") bearerToken: String,
    ): Any

    @POST
    suspend fun removeFavorite(
        @Url url: String,
        @Body toggleFavoriteRequest: ToggleFavoriteRequest,
        @Header("Authorization") bearerToken: String,
    ): Any

    @GET
    suspend fun getAlbumsCount(
        // used to get the total albums value
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") pageSize: Int = 1,
    ): AllAlbumsDto

    @GET
    suspend fun getAllAlbums(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") pageSize: Int = 20,
        @Query("start") startIndex: Int = 0,
        @Query("sortby") sortBy: String,
        @Query("reverse") sortOrder: Int
    ): AllAlbumsDto

    @POST
    suspend fun getAlbumWithInfo(
        @Url url: String,
        @Body albumHashRequest: AlbumHashRequestDto,
        @Header("Authorization") bearerToken: String,
    ): AlbumWithInfoDto

    @GET
    suspend fun getArtistInfo(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("tracklimit") trackLimit: Int = -1,
        @Query("all") returnAllAlbums: Boolean = true,
    ): ArtistInfoDto

    @GET
    suspend fun getSimilarArtists(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): List<ArtistDto>

    @GET
    suspend fun searchAlbums(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = -1,
        @Query("itemtype") itemType: String = "albums",
        @Query("q") searchParams: String
    ): AlbumsSearchResultDto

    @GET
    suspend fun searchArtists(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = -1,
        @Query("itemtype") itemType: String = "artists",
        @Query("q") searchParams: String
    ): ArtistsSearchResultDto

    @GET
    suspend fun searchTracks(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = -1,
        @Query("itemtype") itemType: String = "tracks",
        @Query("q") searchParams: String
    ): TracksSearchResultDto

    @GET
    suspend fun getTopSearchResults(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = -1,
        @Query("q") searchParams: String
    ): TopSearchResultsDto
}
