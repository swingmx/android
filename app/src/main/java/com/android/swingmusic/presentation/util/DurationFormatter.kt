package com.android.swingmusic.presentation.util

internal fun Int.formatDuration(): String {
    val hours = this / 3600
    val remainingSeconds = this % 3600
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    return when {
        hours > 0 -> "%02d:%02d:%02d".format(hours, minutes, seconds)
        minutes > 0 -> "%02d:%02d".format(minutes, seconds)
        else -> "00:%02d".format(seconds)
    }
}
