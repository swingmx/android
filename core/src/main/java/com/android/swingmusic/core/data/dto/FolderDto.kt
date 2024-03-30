package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class FolderDto(
    @SerializedName("count")
    val fileCount: Int?,
    @SerializedName("is_sym")
    val isSym: Boolean?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("path")
    val path: String?
)
