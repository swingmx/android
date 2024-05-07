package com.android.swingmusic.core.data.mapper

import com.android.swingmusic.core.data.dto.AllArtistsDto
import com.android.swingmusic.core.data.dto.ArtistDto
import com.android.swingmusic.core.data.dto.DirDto
import com.android.swingmusic.core.data.dto.DirListDto
import com.android.swingmusic.core.data.dto.FolderDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.dto.RootDirsDto
import com.android.swingmusic.core.data.dto.TrackArtistDto
import com.android.swingmusic.core.data.dto.TrackDto
import com.android.swingmusic.core.domain.model.AllArtists
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Dir
import com.android.swingmusic.core.domain.model.DirList
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.RootDirs
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist

object Map {
    fun ArtistDto.toArtist(): Artist {
        return Artist(
            artisthash = artisthash ?: "",
            name = name ?: "",
            colors = colors ?: emptyList(),
            createdDate = createdDate ?: 0.0,
            helpText = helpText ?: "",
            image = image ?: ""
        )
    }

    fun TrackArtistDto.toArtist(): TrackArtist {
        return TrackArtist(
            artistHash = artistHash ?: "",
            image = image ?: "",
            name = name ?: ""
        )
    }

    fun FolderDto.toFolder(): Folder {
        return Folder(
            trackCount = fileCount ?: 0,
            folderCount = folderCount ?: 0,
            isSym = isSym ?: false,
            name = name ?: "",
            path = path ?: ""
        )
    }

    fun TrackDto.toTrack(): Track {
        return Track(
            album = album,
            albumTrackArtists = albumTrackArtistDtos?.map { it.toArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            artistHashes = artistHashes ?: "",
            trackArtists = artistsDto?.map { it.toArtist() } ?: emptyList(),
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

    fun AllArtistsDto.toAllArtists(): AllArtists {
        return AllArtists(
            artists = artists?.map { it.toArtist() } ?: emptyList(),
            total = total ?: 0
        )
    }

    fun FoldersAndTracksDto.toFolderAndTracks(): FoldersAndTracks {
        return FoldersAndTracks(
            folders = foldersDto?.map { it.toFolder() } ?: emptyList<Folder>(),
            tracks = tracksDto?.map { it.toTrack() } ?: emptyList<Track>()
        )
    }

    fun FoldersAndTracksRequestDto.toFolderAndTracksRequest(): FoldersAndTracksRequest {
        return FoldersAndTracksRequest(
            folder = folder ?: "",
            tracksOnly = tracksOnly ?: false
        )
    }

    fun FoldersAndTracksRequest.toFoldersAndTracksRequestDto(): FoldersAndTracksRequestDto {
        return FoldersAndTracksRequestDto(
            folder = folder,
            tracksOnly = tracksOnly
        )
    }

    fun DirDto.toDir(): Dir {
        return Dir(
            name = name ?: "",
            path = path ?: ""
        )
    }

    fun DirListDto.toDirList(): DirList {
        return DirList(
            folders = folders?.map { it.toDir() } ?: emptyList()
        )
    }

    fun RootDirsDto.toRootDirs(): RootDirs {
        return RootDirs(
            rootDirs = rootDirs ?: emptyList()
        )
    }

    fun RootDirs.toRootDirsDto(): RootDirsDto {
        return RootDirsDto(
            rootDirs = rootDirs
        )
    }
}
