package com.android.swingmusic.network.data.dto

import com.google.gson.annotations.SerializedName

data class AddToPlaylistRequestDto(
    @SerializedName("itemtype")
    val itemType: String,
    @SerializedName("itemhash")
    val itemHash: String
)

data class SaveAsPlaylistRequestDto(
    @SerializedName("itemtype")
    val itemType: String,
    @SerializedName("itemhash")
    val itemHash: String,
    @SerializedName("playlist_name")
    val playlistName: String
)

data class CreatePlaylistRequestDto(
    @SerializedName("name")
    val name: String
)

data class RemovePlaylistTracksRequestDto(
    @SerializedName("tracks")
    val tracks: List<PlaylistTrackRefDto>
)

/** A track by its hash and position in the whole playlist; the server checks both match. */
data class PlaylistTrackRefDto(
    @SerializedName("trackhash")
    val trackHash: String,
    @SerializedName("index")
    val index: Int
)
