package com.android.swingmusic.core.data.mapper

import com.android.swingmusic.core.data.dto.ArtistDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.dto.FolderDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.RootDirDto
import com.android.swingmusic.core.data.dto.RootDirListDto
import com.android.swingmusic.core.data.dto.TrackDto
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.RootDir
import com.android.swingmusic.core.domain.model.RootDirList
import com.android.swingmusic.core.domain.model.Track

object Map {
    fun ArtistDto.toArtist(): Artist {
        return Artist(
            artistHash = artistHash ?: "",
            image = image ?: "",
            name = name ?: ""
        )
    }

    fun FolderDto.toFolder(): Folder {
        return Folder(
            fileCount = fileCount ?: 0,
            isSym = isSym ?: false,
            name = name ?: "",
            path = path ?: ""
        )
    }

    fun TrackDto.toTrack(): Track {
        return Track(
            album = album,
            albumArtists = albumArtistDtos?.map { it.toArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            artistHashes = artistHashes ?: "",
            artists = artistDtos?.map { it.toArtist() } ?: emptyList(),
            ati = ati ?: "",
            bitrate = bitrate ?: 0,
            copyright = copyright ?: "",
            createdDate = createdDate ?: 0.0,
            date = date ?: 0,
            disc = disc ?: 0,
            duration = duration ?: 0,
            filepath = filepath ?: "",
            folder = folder ?: "",
            genre = genre ?: emptyList(),
            image = image ?: "",
            isFavorite = isFavorite ?: false,
            lastMod = lastMod ?: 0,
            ogAlbum = ogAlbum ?: "",
            ogTitle = ogTitle ?: "",
            pos = pos ?: 0,
            title = title ?: "",
            track = track ?: 0,
            trackHash = trackHash ?: ""
        )
    }

    fun FoldersAndTracksDto.toFolderAndTracks(): FoldersAndTracks {
        return FoldersAndTracks(
            folders = foldersDto?.map { it.toFolder() } ?: emptyList<Folder>(),
            tracks = tracksDto?.map { it.toTrack() } ?: emptyList<Track>()
        )
    }

    fun FoldersAndTracksRequestDto.toFolderAndTracksRequest(): FoldersAndTracksRequest{
        return FoldersAndTracksRequest(
            folder = folder ?: "",
            tracksOnly = tracksOnly ?: false
        )
    }

    fun FoldersAndTracksRequest.toFoldersAndTracksRequestDto(): FoldersAndTracksRequestDto{
        return FoldersAndTracksRequestDto(
            folder = folder,
            tracksOnly = tracksOnly
        )
    }

    fun RootDirDto.toRootDir(): RootDir {
        return RootDir(
            name = name ?: "",
            path = path ?: ""
        )
    }

    fun RootDirListDto.toRootDirList(): RootDirList {
        return RootDirList(
            folders = folders?.map { it.toRootDir() } ?: emptyList()
        )
    }
}
