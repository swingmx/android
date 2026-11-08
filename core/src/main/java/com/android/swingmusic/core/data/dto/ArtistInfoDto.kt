package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class ArtistInfoDto(
    @SerializedName("albums")
    val albumsAndAppearancesDto: AlbumsAndAppearancesDto?,
    @SerializedName("artist")
    val artistExpandedDto: ArtistExpandedDto?,
    @SerializedName("tracks")
    val tracks: List<TrackDto>?,
    @SerializedName("stats")
    val stats: List<StatItemDto>?,
    /** The current user's all-time listening; null when nothing has been played. */
    @SerializedName("listening")
    val listening: ListeningDto?
)

data class ListeningDto(
    @SerializedName("playduration")
    val playDuration: Int?,
    @SerializedName("playcount")
    val playCount: Int?,
    @SerializedName("lastplayed_ago")
    val lastPlayedAgo: String?,
    @SerializedName("rank")
    val rank: ListeningRankDto?,
    @SerializedName("top_album")
    val topAlbum: ListeningTopAlbumDto?,
    @SerializedName("top_track")
    val topTrack: ListeningTopTrackDto?
)

data class ListeningRankDto(
    @SerializedName("position")
    val position: Int?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("of")
    val of: String?
)

data class ListeningTopAlbumDto(
    @SerializedName("albumhash")
    val albumHash: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("artist")
    val artist: String?,
    @SerializedName("playduration")
    val playDuration: Int?
)

data class ListeningTopTrackDto(
    @SerializedName("trackhash")
    val trackHash: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("playduration")
    val playDuration: Int?
)

data class StatItemDto(
    @SerializedName("cssclass")
    val cssClass: String?,
    @SerializedName("value")
    val value: String?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("image")
    val image: String?
)
