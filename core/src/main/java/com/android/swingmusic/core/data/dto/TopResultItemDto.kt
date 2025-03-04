package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class TopResultItemDto(
    @SerializedName("type")
    val type: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("albumcount")
    val albumcount: Int?,
    @SerializedName("artisthash")
    val artistHash: String?,
    @SerializedName("trackhash")
    val trackHash: String?,
    @SerializedName("albumhash")
    val albumHash: String?,
    @SerializedName("color")
    val color: String?,
    @SerializedName("created_date")
    val createdDate: Int?,
    @SerializedName("date")
    val date: Int?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("fav_userids")
    val favUserIdsDto: List<String>?,
    @SerializedName("genrehashes")
    val genreHashes: String?,
    @SerializedName("genres")
    val genresDto: List<GenreDto>?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("lastplayed")
    val lastPlayed: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("playcount")
    val playCount: Int?,
    @SerializedName("playduration")
    val playDuration: Int?,
    @SerializedName("trackcount")
    val trackcount: Int?,
    @SerializedName("album")
    val album: String?,
    @SerializedName("albumartists")
    val albumArtists: List<ArtistDto>?,
    @SerializedName("artisthashes")
    val artistHashes: List<String>?,
    @SerializedName("artists")
    val artists: List<ArtistDto>?,
    @SerializedName("bitrate")
    val bitrate: Int?,
    @SerializedName("explicit")
    val explicit: Boolean?,
    @SerializedName("filepath")
    val filepath: String?,
    @SerializedName("folder")
    val folder: String?,
    @SerializedName("is_favorite")
    val isFavorite: Boolean?
)
