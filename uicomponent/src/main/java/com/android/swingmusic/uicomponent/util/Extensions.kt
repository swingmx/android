package com.android.swingmusic.uicomponent.util

fun Int.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "$minutes:$seconds"
}

fun String.trimString(limit: Int): String {
    return if (this.length <= limit) this else this.take(limit - 1).plus("...")
}
