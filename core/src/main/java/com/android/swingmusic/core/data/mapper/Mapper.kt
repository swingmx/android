package com.android.swingmusic.core.data.mapper

import com.android.swingmusic.core.data.dto.AlbumDto
import com.android.swingmusic.core.data.dto.AlbumInfoDto
import com.android.swingmusic.core.data.dto.AlbumResultDto
import com.android.swingmusic.core.data.dto.AlbumWithInfoDto
import com.android.swingmusic.core.data.dto.AlbumsAndAppearancesDto
import com.android.swingmusic.core.data.dto.AlbumsSearchResultDto
import com.android.swingmusic.core.data.dto.AllAlbumsDto
import com.android.swingmusic.core.data.dto.AllArtistsDto
import com.android.swingmusic.core.data.dto.ArtistDto
import com.android.swingmusic.core.data.dto.ArtistExpandedDto
import com.android.swingmusic.core.data.dto.ArtistInfoDto
import com.android.swingmusic.core.data.dto.ArtistResultDto
import com.android.swingmusic.core.data.dto.ArtistsSearchResultDto
import com.android.swingmusic.core.data.dto.DirDto
import com.android.swingmusic.core.data.dto.DirListDto
import com.android.swingmusic.core.data.dto.FolderDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksDto
import com.android.swingmusic.core.data.dto.FoldersAndTracksRequestDto
import com.android.swingmusic.core.data.dto.GenreDto
import com.android.swingmusic.core.data.dto.RootDirsDto
import com.android.swingmusic.core.data.dto.TopResultItemDto
import com.android.swingmusic.core.data.dto.TopSearchResultsDto
import com.android.swingmusic.core.data.dto.TrackArtistDto
import com.android.swingmusic.core.data.dto.TrackDto
import com.android.swingmusic.core.data.dto.TrackResultDto
import com.android.swingmusic.core.data.dto.TracksSearchResultDto
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.AlbumInfo
import com.android.swingmusic.core.domain.model.AlbumWithInfo
import com.android.swingmusic.core.domain.model.AlbumsAndAppearances
import com.android.swingmusic.core.domain.model.AlbumsSearchResult
import com.android.swingmusic.core.domain.model.AllAlbums
import com.android.swingmusic.core.domain.model.AllArtists
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.ArtistExpanded
import com.android.swingmusic.core.domain.model.ArtistInfo
import com.android.swingmusic.core.domain.model.ArtistsSearchResult
import com.android.swingmusic.core.domain.model.Dir
import com.android.swingmusic.core.domain.model.DirList
import com.android.swingmusic.core.domain.model.Folder
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.domain.model.Genre
import com.android.swingmusic.core.domain.model.RootDirs
import com.android.swingmusic.core.domain.model.TopResultItem
import com.android.swingmusic.core.domain.model.TopSearchResults
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.core.domain.model.TrackArtist
import com.android.swingmusic.core.domain.model.TracksSearchResult

object Map {
    fun ArtistDto.toArtist(): Artist {
        return Artist(
            artistHash = artisthash ?: "",
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

    fun TrackDto.toTrack(): Track {
        return Track(
            album = album ?: "",
            albumTrackArtists = albumTrackArtistDto?.map { it.toArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            trackArtists = artistsDto?.map { it.toArtist() } ?: emptyList(),
            bitrate = bitrate ?: 0,
            duration = duration ?: 0,
            filepath = filepath ?: "",
            folder = folder ?: "",
            image = image ?: "",
            isFavorite = isFavorite ?: false,
            title = title ?: "",
            trackHash = trackHash ?: "",
            disc = disc ?: 1,
            trackNumber = trackNumber ?: 1,
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
            tracksOnly = tracksOnly,
            limit = limit
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
            tracks = tracks?.map { it.toTrack() } ?: emptyList(),
            copyright = copyright ?: ""
        )
    }

    private fun AlbumsAndAppearancesDto.toAlbumsAndAppearances(): AlbumsAndAppearances {
        return AlbumsAndAppearances(
            albums = albums?.map { it.toAlbum() } ?: emptyList(),
            appearances = appearances?.map { it.toAlbum() } ?: emptyList(),
            artistName = artistName ?: "",
            compilations = compilations?.map { it.toAlbum() } ?: emptyList(),
            singlesAndEps = singlesAndEps?.map { it.toAlbum() } ?: emptyList()
        )
    }

    private fun ArtistExpandedDto.toArtistExpanded(): ArtistExpanded {
        return ArtistExpanded(
            albumCount = albumcount ?: 0,
            artistHash = artisthash ?: "",
            color = color ?: "",
            duration = duration ?: 0,
            genres = genres?.map { it.toGenre() } ?: emptyList(),
            image = image ?: "",
            isFavorite = isFavorite ?: false,
            name = name ?: "",
            trackCount = trackcount ?: 0
        )
    }

    fun ArtistInfoDto.toArtistInfo(): ArtistInfo {
        return ArtistInfo(
            albumsAndAppearances = albumsAndAppearancesDto?.toAlbumsAndAppearances()
                ?: AlbumsAndAppearances(
                    albums = emptyList(),
                    appearances = emptyList(),
                    artistName = "",
                    compilations = emptyList(),
                    singlesAndEps = emptyList()
                ),
            artist = artistExpandedDto?.toArtistExpanded() ?: ArtistExpanded(
                albumCount = 0,
                artistHash = "",
                color = "",
                duration = 0,
                genres = emptyList(),
                image = "",
                isFavorite = false,
                name = "",
                trackCount = 0
            ),
            tracks = tracks?.map { it.toTrack() } ?: emptyList()
        )
    }

    // Search Mappers -->

    private fun AlbumResultDto.toAlbum(): Album {
        return Album(
            albumArtists = albumArtists?.map { it.toArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            colors = listOf(color ?: "#FFFFFF"),
            createdDate = System.currentTimeMillis().toDouble(),
            date = date ?: 0,
            helpText = "",
            image = image ?: "",
            title = title ?: "Untitled",
            versions = versions?.filterIsInstance<String>() ?: emptyList()
        )
    }

    fun AlbumsSearchResultDto.toAlbumsSearchResult(): AlbumsSearchResult {
        return AlbumsSearchResult(
            more = more ?: false,
            result = results?.map { it.toAlbum() } ?: emptyList()
        )
    }

    private fun ArtistResultDto.toArtist(): Artist {
        return Artist(
            artistHash = artisthash ?: "",
            colors = listOf(color ?: "#FFFFFF"),
            createdDate = createdDate?.toDouble() ?: 0.0,
            helpText = "",
            image = image ?: "",
            name = name ?: "Unknown Artist"
        )
    }

    fun ArtistsSearchResultDto.toArtistsSearchResult(): ArtistsSearchResult {
        return ArtistsSearchResult(
            more = more ?: false,
            results = resultDto?.map { it.toArtist() } ?: emptyList()
        )
    }

    private fun ArtistDto.toTrackArtist(): TrackArtist {
        return TrackArtist(
            name = name ?: "Unknown Artist",
            artistHash = artisthash ?: "",
            image = image ?: ""
        )
    }

    private fun TrackResultDto.toTrackResults(): Track {
        return Track(
            album = album ?: "",
            albumTrackArtists = albumArtists?.map { it.toTrackArtist() } ?: emptyList(),
            albumHash = albumHash ?: "",
            trackArtists = artists?.map { it.toArtist() } ?: emptyList(),
            bitrate = bitrate ?: 0,
            duration = duration ?: 0,
            filepath = filepath ?: "",
            folder = folder ?: "",
            image = image ?: "",
            isFavorite = isFavorite ?: false,
            title = title ?: "Untitled",
            trackHash = trackHash ?: "",
            disc = 0,
            trackNumber = 0
        )
    }

    fun TracksSearchResultDto.toTracksSearchResult(): TracksSearchResult {
        return TracksSearchResult(
            more = more ?: false,
            results = results?.map { it.toTrackResults() } ?: emptyList()
        )
    }

    private fun TopResultItemDto?.toTopResultItem(): TopResultItem? {
        if (this == null) return null

        return TopResultItem(
            type = type ?: "",
            title = title ?: "",
            albumCount = albumcount ?: 0,
            artistHash = artistHash ?: "",
            albumHash = albumHash ?: "",
            trackHash = trackHash ?: "",
            genres = genresDto?.map { it.toGenre() } ?: emptyList(),
            trackCount = trackcount ?: 0,
            albumcount = albumcount ?: 0,
            color = color ?: "",
            createdDate = createdDate ?: 0,
            date = date ?: 0,
            duration = duration ?: 0,
            favUserIds = favUserIdsDto ?: emptyList(),
            genreHashes = genreHashes ?: "",
            id = id ?: 0,
            image = image ?: "",
            lastPlayed = lastPlayed ?: 0,
            name = name ?: "",
            playCount = playCount ?: 0,
            playDuration = playDuration ?: 0,
            trackcount = trackcount ?: 0,
            album = album ?: "",
            albumArtists = albumArtists?.map { it.toArtist() } ?: emptyList(),
            artistHashes = artistHashes ?: emptyList(),
            artists = artists?.map { it.toArtist() } ?: emptyList(),
            bitrate = bitrate ?: 0,
            explicit = explicit ?: false,
            filepath = filepath ?: "",
            folder = folder ?: "",
            isFavorite = isFavorite ?: false
        )
    }

    fun TopSearchResultsDto.toTopSearchResults(): TopSearchResults {
        return TopSearchResults(
            topResultItem = topResultItemDto?.toTopResultItem(),
            tracks = tracksDto?.map { it.toTrack() } ?: emptyList(),
            albums = albumsDto?.map { it.toAlbum() } ?: emptyList(),
            artists = artistsDto?.map { it.toArtist() } ?: emptyList()
        )
    }
}
