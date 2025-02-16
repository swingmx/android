package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class ArtistResultDto(
    @SerializedName("albumcount")
    val albumcount: Int?,
    @SerializedName("artisthash")
    val artisthash: String?,
    @SerializedName("color")
    val color: String?,
    @SerializedName("created_date")
    val createdDate: Int?,
    @SerializedName("date")
    val date: Int?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("fav_userids")
    val favUserIdsDto: List<Any>?,
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
    val trackcount: Int?
)
