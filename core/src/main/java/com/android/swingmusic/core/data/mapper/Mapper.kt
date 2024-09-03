package com.android.swingmusic.core.data.mapper

import com.android.swingmusic.core.data.dto.AlbumDto
import com.android.swingmusic.core.data.dto.AlbumInfoDto
import com.android.swingmusic.core.data.dto.AlbumWithInfoDto
import com.android.swingmusic.core.data.dto.AllAlbumsDto
import com.android.swingmusic.core.data.dto.AllArtistsDto
import com.android.swingmusic.core.data.dto.ArtistDto
import com.android.swingmusic.core.data.dto.DirDto
import com.android.swingmusic.core.data.dto.DirListDto
import com.android.swingmusic.core.data.dto.FolderDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.dto.GenreDto
import com.android.swingmusic.core.data.dto.RootDirsDto
import com.android.swingmusic.core.data.dto.TrackArtistDto
import com.android.swingmusic.core.data.dto.TrackDto
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.AlbumInfo
import com.android.swingmusic.core.domain.model.AlbumWithInfo
import com.android.swingmusic.core.domain.model.AllAlbums
import com.android.swingmusic.core.domain.model.AllArtists
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Dir
import com.android.swingmusic.core.domain.model.DirList
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.Genre
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

    private fun FolderDto.toFolder(): Folder {
        return Folder(
            trackCount = fileCount ?: 0,
            folderCount = folderCount ?: 0,
            isSym = isSym ?: false,
            name = name ?: "",
            path = path ?: ""
        )
    }

    private fun TrackDto.toTrack(): Track {
        return Track(
            album = album ?: "",
            albumTrackArtists = albumTrackArtistDtos?.map { it.toArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            artistHashes = artistHashes ?: "",
            trackArtists = artistsDto?.map { it.toArtist() } ?: emptyList(),
            bitrate = bitrate ?: 0,
            duration = duration ?: 0,
            filepath = filepath ?: "",
            folder = folder ?: "",
            image = image ?: "",
            isFavorite = isFavorite ?: false,
            title = title ?: "",
            trackHash = trackHash ?: ""
        )
    }

    fun AllArtistsDto.toAllArtists(): AllArtists {
        return AllArtists(
            artists = artistsDto?.map { it.toArtist() } ?: emptyList(),
            total = total ?: 0
        )
    }

    fun FoldersAndTracksDto.toModel(): FoldersAndTracks {
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

    private fun DirDto.toDir(): Dir {
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

    fun AlbumDto.toAlbum(): Album {
        return Album(
            albumArtists = albumArtistDto?.map { it.toArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            colors = colors ?: emptyList(),
            createdDate = createdDate ?: 0.0,
            date = date ?: 0,
            helpText = helpText ?: "",
            image = image ?: "",
            title = title ?: "",
            versions = versions ?: emptyList()
        )
    }

    fun AllAlbumsDto.toAllAlbums(): AllAlbums {
        return AllAlbums(
            albums = albumDto?.map { it.toAlbum() } ?: emptyList(),
            total = total ?: 0
        )
    }

    private fun GenreDto.toGenre(): Genre {
        return Genre(
            genreHash = genreHash ?: "",
            name = name ?: ""
        )
    }


    private fun AlbumInfoDto.toAlbumInfo(): AlbumInfo {
        return AlbumInfo(
            albumArtists = albumArtists?.map { it.toArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            artistHashes = artistHashes ?: emptyList(),
            baseTitle = baseTitle ?: "",
            color = color ?: "",
            createdDate = createdDate ?: 0,
            date = date ?: 0,
            duration = duration ?: 0,
            favUserIds = favUserIds ?: emptyList(),
            genreHashes = genreHashes ?: "",
            genres = genreDto?.map { it.toGenre() } ?: emptyList(),
            id = id ?: 0,
            image = image ?: "",
            isFavorite = isFavorite ?: false,
            lastPlayed = lastPlayed ?: 0,
            ogTitle = ogTitle ?: "",
            playCount = playCount ?: 0,
            playDuration = playDuration ?: 0,
            title = title ?: "",
            trackCount = trackcount ?: 0,
            type = type ?: "",
            versions = versions ?: emptyList()
        )
    }


    fun AlbumWithInfoDto.toAlbumWithInfo(): AlbumWithInfo {
        return AlbumWithInfo(
            albumInfo = albumInfoDto?.toAlbumInfo() ?: AlbumInfo(
                albumArtists = emptyList(),
                albumHash = "",
                artistHashes = emptyList(),
                baseTitle = "",
                color = "",
                createdDate = 0,
                date = 0,
                duration = 0,
                favUserIds = emptyList(),
                genreHashes = "",
                genres = emptyList(),
                id = 0,
                image = "",
                isFavorite = false,
                lastPlayed = 0,
                ogTitle = "",
                playCount = 0,
                playDuration = 0,
                title = "Swing Music",
                trackCount = 0,
                type = "",
                versions = emptyList()
            ),
            tracks = tracks?.map { it.toTrack() } ?: emptyList()
        )
    }
}
