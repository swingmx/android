package com.android.swingmusic.database.data.mapper

import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.database.data.entity.BaseUrlEntity
import com.android.swingmusic.database.data.entity.LastPlayedTrackEntity
import com.android.swingmusic.database.data.entity.QueueEntity
import com.android.swingmusic.database.data.entity.TrackArtistEntity
import com.android.swingmusic.database.data.entity.UserEntity
import com.android.swingmusic.database.domain.model.BaseUrl
import com.android.swingmusic.database.domain.model.LastPlayedTrack
import com.android.swingmusic.database.domain.model.User

fun BaseUrl.toEntity(): BaseUrlEntity {
    return BaseUrlEntity(url = url)
}

fun BaseUrlEntity.toModel(): BaseUrl {
    return BaseUrl(id = id, url = url)
}

fun UserEntity.toModel(): User {
    return User(
        id = id,
        firstname = firstname,
        lastname = lastname,
        email = email,
        username = username,
        image = image,
        roles = roles
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        firstname = firstname,
        lastname = lastname,
        email = email,
        username = username,
        image = image,
        roles = roles
    )
}

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
        trackArtists = this.trackArtists.map { it.toEntity() },
        disc = this.disc,
        trackNumber = this.trackNumber
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
        trackArtists = this.trackArtists.map { it.toModel() },
        disc = this.disc,
        trackNumber = this.trackNumber
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
        indexInQueue = this.indexInQueue,
        source = this.source,
        lastPlayPositionMs = lastPlayPositionMs
    )
}

fun LastPlayedTrackEntity.toModel(): LastPlayedTrack {
    return LastPlayedTrack(
        trackHash = this.trackHash,
        indexInQueue = this.indexInQueue,
        source = this.source,
        lastPlayPositionMs = this.lastPlayPositionMs
    )
}
