package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("album")
    val album: String?,
    @SerializedName("albumartists")
    val albumTrackArtistDtos: List<TrackArtistDto>?,
    @SerializedName("albumhash")
    val albumHash: String?,
    @SerializedName("artist_hashes")
    val artistHashes: String?,
    @SerializedName("artists")
    val artistsDto: List<TrackArtistDto>?,
    @SerializedName("_ati")
    val ati: String?,
    @SerializedName("bitrate")
    val bitrate: Int?,
    @SerializedName("copyright")
    val copyright: String?,
    @SerializedName("created_date")
    val createdDate: Double?,
    @SerializedName("date")
    val date: Int?,
    @SerializedName("disc")
    val disc: Int?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("filepath")
    val filepath: String?,
    @SerializedName("folder")
    val folder: String?,
    @SerializedName("genre")
    val genre: List<String>?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("is_favorite")
    val isFavorite: Boolean?,
    @SerializedName("last_mod")
    val lastMod: Int?,
    @SerializedName("og_album")
    val ogAlbum: String?,
    @SerializedName("og_title")
    val ogTitle: String?,
    @SerializedName("_pos")
    val pos: Int?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("track")
    val track: Int?,
    @SerializedName("trackhash")
    val trackHash: String?
)
