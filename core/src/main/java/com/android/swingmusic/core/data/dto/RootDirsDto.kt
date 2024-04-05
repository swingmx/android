package com.android.swingmusic.core.data.dto


import com.google.gson.annotations.SerializedName

data class RootDirsDto(
    @SerializedName("root_dirs", alternate = ["dirs"])
    val rootDirs: List<String>?
)
