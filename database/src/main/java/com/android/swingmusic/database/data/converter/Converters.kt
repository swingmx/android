package com.android.swingmusic.database.data.converter

import androidx.room.TypeConverter
import com.android.swingmusic.core.domain.util.QueueSource
import com.android.swingmusic.database.data.entity.TrackArtistEntity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson: Gson

    init {
        val gsonBuilder = GsonBuilder()
        gson = gsonBuilder.create()
    }

    @TypeConverter
    fun fromTrackArtistList(value: List<TrackArtistEntity>): String {
        val type = object : TypeToken<List<TrackArtistEntity>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toTrackArtistList(value: String): List<TrackArtistEntity> {
        val type = object : TypeToken<List<TrackArtistEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    // Used for User roles
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromQueueSource(queueSource: QueueSource): String {
        return when (queueSource) {
            is QueueSource.ALBUM -> "ALBUM|${queueSource.albumHash.escape()}|${queueSource.name.escape()}"
            is QueueSource.ARTIST -> "ARTIST|${queueSource.artistHash.escape()}|${queueSource.name.escape()}"
            is QueueSource.FOLDER -> "FOLDER|${queueSource.path.escape()}|${queueSource.name.escape()}"
            is QueueSource.PLAYLIST -> "PLAYLIST|${queueSource.id.escape()}|${queueSource.name.escape()}"
            is QueueSource.QUERY -> "QUERY|${queueSource.query.escape()}|${queueSource.name.escape()}"
            is QueueSource.FAVORITE -> "FAVORITE"
            is QueueSource.UNKNOWN -> "UNKNOWN"
            else -> ""
        }
    }

    @TypeConverter
    fun toQueueSource(value: String): QueueSource {
        val parts = value.split("|").map { it.unescape() }
        return when (parts[0]) {
            "ALBUM" -> QueueSource.ALBUM(parts[1], parts[2])
            "ARTIST" -> QueueSource.ARTIST(parts[1], parts[2])
            "FOLDER" -> QueueSource.FOLDER(parts[1], parts[2])
            "PLAYLIST" -> QueueSource.PLAYLIST(parts[1], parts[2])
            "QUERY" -> QueueSource.QUERY(parts[1], parts[2])
            "FAVORITE" -> QueueSource.FAVORITE
            "UNKNOWN" -> QueueSource.UNKNOWN
            else -> throw IllegalArgumentException("Unknown QueueSource type")
        }
    }

    // Extension function to escape the delimiter in values
    private fun String.escape(): String {
        return this.replace("|", "\\|")
    }

    // Extension function to unescape the delimiter in values
    private fun String.unescape(): String {
        return this.replace("\\|", "|")
    }
}
