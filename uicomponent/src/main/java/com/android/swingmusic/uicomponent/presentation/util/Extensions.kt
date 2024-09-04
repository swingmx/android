package com.android.swingmusic.uicomponent.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Int.formatDuration(): String {
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

fun Int.formattedAlbumDuration(): String {
    val totalMinutes = this / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val seconds = this % 60

    return buildString {
        if (hours > 0) {
            append("${hours}hr")
            if (hours > 1) append("s")
        }
        if (minutes > 0) {
            if (hours > 0) append(" ")
            append("${minutes}min")
        }
        if (seconds > 0) {
            if (hours > 0 || minutes > 0) append(" ")
            append("${seconds}sec")
        }
    }.ifEmpty { "0sec" }
}


fun Long.formatDate(pattern: String): String {
    val instant = Instant.ofEpochSecond(this)
    val formatter = DateTimeFormatter.ofPattern(pattern)
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
