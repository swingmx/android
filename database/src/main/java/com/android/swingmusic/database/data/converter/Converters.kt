package com.android.swingmusic.database.data.converter

import androidx.room.TypeConverter
import com.android.swingmusic.database.data.entity.TrackArtistEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromTrackArtistList(value: List<TrackArtistEntity>): String {
        val gson = Gson()
        val type = object : TypeToken<List<TrackArtistEntity>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toTrackArtistList(value: String): List<TrackArtistEntity> {
        val gson = Gson()
        val type = object : TypeToken<List<TrackArtistEntity>>() {}.type
        return gson.fromJson(value, type)
    }

    // Used for User roles
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}
