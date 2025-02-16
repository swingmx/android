package com.android.swingmusic.uicomponent.presentation.util

import com.android.swingmusic.core.domain.util.QueueSource
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
    val totalMinutes = (this + 30) / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return buildString {
        if (hours > 0) {
            append("${hours}hr")
            if (hours > 1) append("s")
        }
        if (minutes > 0) {
            if (hours > 0) append(" ")
            append("${minutes}min")
        }
    }.ifEmpty { "0min" }
}


fun Long.formatDate(pattern: String): String {
    val instant = Instant.ofEpochSecond(this)
    val formatter = DateTimeFormatter.ofPattern(pattern)
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

fun QueueSource.getSourceType(): String {
    return when (this) {
        is QueueSource.ALBUM -> "Album"
        is QueueSource.ARTIST -> "Artist"
        is QueueSource.FOLDER -> "Folder"
        is QueueSource.PLAYLIST -> "Playlist"
        is QueueSource.SEARCH -> "Search"
        QueueSource.FAVORITE -> "Favorite"
        QueueSource.UNKNOWN -> "Unknown"
        else -> "Unknown"
    }
}

fun QueueSource.getName(): String {
    return when (this) {
        is QueueSource.ALBUM -> this.name
        is QueueSource.ARTIST -> this.name
        is QueueSource.FOLDER -> this.name
        is QueueSource.PLAYLIST -> this.name
        else -> ""
    }
}
