package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class FolderDto(
    @SerializedName("count", alternate = ["trackcount"])
    val fileCount: Int?,
    @SerializedName("foldercount")
    val folderCount: Int?,
    @SerializedName("is_sym")
    val isSym: Boolean?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("path")
    val path: String?
)
