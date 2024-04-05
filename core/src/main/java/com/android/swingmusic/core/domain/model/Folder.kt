package com.android.swingmusic.core.domain.model


data class Folder(
    val trackCount: Int,
    val folderCount: Int,
    val isSym: Boolean,
    val name: String,
    val path: String
)
