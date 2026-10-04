package com.android.swingmusic.network.data.api.service

import com.android.swingmusic.core.data.dto.TriggerScanRequestDto
import com.android.swingmusic.core.data.dto.ChartResponseDto
import com.android.swingmusic.core.data.dto.PairCodeDto
import com.android.swingmusic.core.data.dto.ServerSettingsDto
import com.android.swingmusic.core.data.dto.WeeklyStatsDto
import com.android.swingmusic.core.data.dto.AlbumWithInfoDto
import com.android.swingmusic.core.data.dto.AlbumsSearchResultDto
import com.android.swingmusic.core.data.dto.AllAlbumsDto
import com.android.swingmusic.core.data.dto.AllArtistsDto
import com.android.swingmusic.core.data.dto.ArtistDto
import com.android.swingmusic.core.data.dto.ArtistInfoDto
import com.android.swingmusic.core.data.dto.ArtistsSearchResultDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.dto.HomeSectionDto
import com.android.swingmusic.core.data.dto.LyricsCheckDto
import com.android.swingmusic.core.data.dto.LyricsDto
import com.android.swingmusic.core.data.dto.LyricsRequestDto
import com.android.swingmusic.core.data.dto.MixTracksDto
import com.android.swingmusic.core.data.dto.PlaylistTracksDto
import com.android.swingmusic.core.data.dto.PluginLyricsRequestDto
import com.android.swingmusic.core.data.dto.PluginLyricsResultDto
import com.android.swingmusic.core.data.dto.TopSearchResultsDto
import com.android.swingmusic.core.data.dto.TrackDto
import com.android.swingmusic.core.data.dto.TracksSearchResultDto
import com.android.swingmusic.core.data.dto.RootDirsDto
import com.android.swingmusic.network.data.dto.AlbumHashRequestDto
import com.android.swingmusic.network.data.dto.LogTrackRequestDto
import com.android.swingmusic.network.data.dto.ToggleFavoriteRequest
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

    @GET
    suspend fun getRootDirectories(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): RootDirsDto

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
    suspend fun getTopSearchResults(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = 5,
        @Query("q") searchParams: String
    ): TopSearchResultsDto

    @GET
    suspend fun getArtistTracks(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): List<TrackDto>

    @GET
    suspend fun getAlbumTracks(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): List<TrackDto>

    @GET
    suspend fun searchAllTracks(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = -1,
        @Query("itemtype") itemType: String = "tracks",
        @Query("q") searchParams: String
    ): TracksSearchResultDto

    @GET
    suspend fun searchAllAlbums(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = -1,
        @Query("itemtype") itemType: String = "albums",
        @Query("q") searchParams: String
    ): AlbumsSearchResultDto

    @GET
    suspend fun searchAllArtists(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int = -1,
        @Query("itemtype") itemType: String = "artists",
        @Query("q") searchParams: String
    ): ArtistsSearchResultDto

    @POST
    suspend fun getLyrics(
        @Url url: String,
        @Body request: LyricsRequestDto,
        @Header("Authorization") bearerToken: String
    ): LyricsDto

    @POST
    suspend fun checkLyricsExist(
        @Url url: String,
        @Body request: LyricsRequestDto,
        @Header("Authorization") bearerToken: String
    ): LyricsCheckDto

    @POST
    suspend fun searchLyricsOnline(
        @Url url: String,
        @Body request: PluginLyricsRequestDto,
        @Header("Authorization") bearerToken: String
    ): PluginLyricsResultDto

    @GET
    suspend fun getHome(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int
    ): List<Map<String, HomeSectionDto>>

    @GET
    suspend fun getPlaylistTracks(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("limit") limit: Int
    ): PlaylistTracksDto

    @GET
    suspend fun getMixTracks(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("mixid") mixId: String,
        @Query("sourcehash") sourceHash: String,
        @Query("og_sourcehash") ogSourceHash: String
    ): MixTracksDto

    @GET
    suspend fun getWeeklyStats(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): WeeklyStatsDto

    @GET
    suspend fun getPairCode(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): PairCodeDto

    @GET
    suspend fun getServerSettings(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): ServerSettingsDto

    /** /logger/top-tracks, /logger/top-artists or /logger/top-albums */
    @GET
    suspend fun getChart(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Query("duration") duration: String,
        @Query("limit") limit: Int,
        @Query("order_by") orderBy: String
    ): ChartResponseDto

    @POST
    suspend fun triggerScan(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Body body: TriggerScanRequestDto
    )
}
