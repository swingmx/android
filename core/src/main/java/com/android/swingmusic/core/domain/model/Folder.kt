package com.android.swingmusic.core.domain.model


data class Folder(
    val fileCount: Int,
    val isSym: Boolean,
    val name: String,
    val path: String
)
