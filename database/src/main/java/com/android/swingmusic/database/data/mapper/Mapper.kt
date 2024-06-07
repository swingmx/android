package com.android.swingmusic.database.data.mapper

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.database.data.entity.LastPlayedTrackEntity
import com.android.swingmusic.database.data.entity.TrackArtistEntity
import com.android.swingmusic.database.data.entity.QueueEntity
import com.android.swingmusic.database.domain.model.LastPlayedTrack

fun Track.toEntity(): QueueEntity {
    return QueueEntity(
        trackHash = this.trackHash,
        album = this.album,
        albumHash = this.albumHash,
        artistHashes = this.artistHashes,
        bitrate = this.bitrate,
        duration = this.duration,
        filepath = this.filepath,
        folder = this.folder,
        image = this.image,
        isFavorite = this.isFavorite,
        title = this.title,
        albumTrackArtists = this.albumTrackArtists.map { it.toEntity() },
        trackArtists = this.trackArtists.map { it.toEntity() }
    )
}

fun TrackArtist.toEntity(): TrackArtistEntity {
    return TrackArtistEntity(
        artistHash = this.artistHash,
        image = this.image,
        name = this.name
    )
}

fun QueueEntity.toModel(): Track {
    return Track(
        trackHash = this.trackHash,
        album = this.album,
        albumHash = this.albumHash,
        artistHashes = this.artistHashes,
        bitrate = this.bitrate,
        duration = this.duration,
        filepath = this.filepath,
        folder = this.folder,
        image = this.image,
        isFavorite = this.isFavorite,
        title = this.title,
        albumTrackArtists = this.albumTrackArtists.map { it.toModel() },
        trackArtists = this.trackArtists.map { it.toModel() }
    )
}

fun TrackArtistEntity.toModel(): TrackArtist {
    return TrackArtist(
        artistHash = this.artistHash,
        image = this.image,
        name = this.name
    )
}

fun LastPlayedTrack.toEntity(): LastPlayedTrackEntity {
    return LastPlayedTrackEntity(
        trackHash = this.trackHash,
        indexInQueue = this.indexInQueue
    )
}

fun LastPlayedTrackEntity.toModel(): LastPlayedTrack {
    return LastPlayedTrack(
        trackHash = this.trackHash,
        indexInQueue = this.indexInQueue
    )
}
