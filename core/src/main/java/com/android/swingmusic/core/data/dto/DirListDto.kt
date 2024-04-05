package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class DirListDto(
    @SerializedName(value = "folders")
    val folders: List<DirDto>?
)
