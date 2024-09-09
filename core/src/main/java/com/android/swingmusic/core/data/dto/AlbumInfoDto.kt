package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class AlbumInfoDto(
    @SerializedName("albumartists")
    val albumArtists: List<ArtistDto>?,
    @SerializedName("albumhash")
    val albumHash: String?,
    @SerializedName("artisthashes")
    val artistHashes: List<String>?,
    @SerializedName("base_title")
    val baseTitle: String?,
    @SerializedName("color")
    val color: String?,
    @SerializedName("created_date")
    val createdDate: Int?,
    @SerializedName("date")
    val date: Long?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("fav_userids")
    val favUserIds: List<Any>?,
    @SerializedName("genrehashes")
    val genreHashes: String?,
    @SerializedName("genres")
    val genreDto: List<GenreDto>?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("is_favorite")
    val isFavorite: Boolean?,
    @SerializedName("lastplayed")
    val lastPlayed: Int?,
    @SerializedName("og_title")
    val ogTitle: String?,
    @SerializedName("playcount")
    val playCount: Int?,
    @SerializedName("playduration")
    val playDuration: Int?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("trackcount")
    val trackcount: Int?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("versions")
    val versions: List<String>?
)
