package com.android.swingmusic.core.data.dto


import com.android.swingmusic.core.data.dto.AlbumDto
import com.google.gson.annotations.SerializedName

data class AlbumsAndAppearancesDto(
    @SerializedName("albums")
    val albums: List<AlbumDto>?,
    @SerializedName("appearances")
    val appearances: List<AlbumDto>?,
    @SerializedName("artistname")
    val artistName: String?,
    @SerializedName("compilations")
    val compilations: List<AlbumDto>?,
    @SerializedName("singles_and_eps")
    val singlesAndEps: List<AlbumDto>?
)
