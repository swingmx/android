package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class RootDirListDto(
    @SerializedName("folders")
    val folders: List<RootDirDto>?
)