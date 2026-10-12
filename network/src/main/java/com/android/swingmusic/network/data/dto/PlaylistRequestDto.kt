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
