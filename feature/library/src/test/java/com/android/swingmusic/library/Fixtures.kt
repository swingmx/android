package com.android.swingmusic.library

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Album
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.Playlist
import com.android.swingmusic.core.domain.model.Track
import com.android.swingmusic.library.domain.FavouriteType
import com.android.swingmusic.library.domain.FavouritesPage
import com.android.swingmusic.library.domain.FavouritesRepository
import com.android.swingmusic.library.domain.FavouritesSummary
import com.android.swingmusic.library.domain.LibraryCounts
import com.android.swingmusic.library.domain.LibraryRepository
import com.android.swingmusic.playlist.domain.PlaylistPage
import com.android.swingmusic.playlist.domain.PlaylistRepository
import com.android.swingmusic.playlist.domain.PlaylistTarget

fun track(hash: String, favourite: Boolean = true) = Track(
    album = "Album", albumTrackArtists = emptyList(), albumHash = "album-$hash", trackArtists = emptyList(),
    bitrate = 320, duration = 180, filepath = "/music/$hash.flac", folder = "/music", image = "",
    isFavorite = favourite, title = "Track $hash", trackHash = hash, disc = 1, trackNumber = 1
)

fun album(hash: String) = Album(
    albumArtists = emptyList(), albumHash = hash, colors = emptyList(), createdDate = 0.0, date = 0,
    helpText = "", image = "", title = "Album $hash", versions = emptyList()
)

fun artist(hash: String) = Artist(
    artistHash = hash, colors = emptyList(), createdDate = 0.0, helpText = "", image = "", name = "Artist $hash"
)

fun playlist(id: String, pinned: Boolean = false) = Playlist(
    id = id, name = "Playlist $id", customImage = null, images = emptyList(), trackCount = 3, pinned = pinned
)

class FakeLibraryRepository : LibraryRepository {
    override suspend fun getBaseUrl() = "https://server/"
    override suspend fun getCounts() = LibraryCounts(playlists = 2, albums = 10, artists = 5)
    override suspend fun getRecentlyAddedAlbums(): Resource<List<Album>> = Resource.Success(emptyList())
}

class FakeFavouritesRepository(
    var tracks: MutableList<Track> = mutableListOf(track("a"), track("b"), track("c")),
    var setFavouriteResult: Resource<Unit> = Resource.Success(Unit),
) : FavouritesRepository {
    val setFavouriteCalls = mutableListOf<Pair<String, Boolean>>()

    override suspend fun getSummary(trackLimit: Int, albumLimit: Int, artistLimit: Int) = Resource.Success(
        FavouritesSummary(
            tracks = tracks.take(trackLimit), albums = listOf(album("x")), artists = listOf(artist("y")),
            trackCount = tracks.size, albumCount = 1, artistCount = 1
        )
    )

    override suspend fun getTracks(start: Int, limit: Int): Resource<FavouritesPage<Track>> {
        val end = if (limit < 0) tracks.size else minOf(tracks.size, start + limit)
        return Resource.Success(FavouritesPage(tracks.subList(start.coerceAtMost(end), end).toList(), tracks.size.takeIf { start == 0 }))
    }

    override suspend fun getAlbums(start: Int, limit: Int) = Resource.Success(FavouritesPage(listOf(album("x")), 1))

    override suspend fun getArtists(start: Int, limit: Int) = Resource.Success(FavouritesPage(listOf(artist("y")), 1))

    override suspend fun setFavourite(hash: String, type: FavouriteType, isFavourite: Boolean): Resource<Unit> {
        setFavouriteCalls += hash to isFavourite
        if (setFavouriteResult is Resource.Success) {
            if (isFavourite) {
                if (tracks.none { it.trackHash == hash }) tracks.add(0, track(hash))
            } else {
                tracks.removeAll { it.trackHash == hash }
            }
        }
        return setFavouriteResult
    }
}

class FakePlaylistRepository(
    var playlists: List<Playlist> = listOf(playlist("1"), playlist("2", pinned = true), playlist("3")),
    var createResult: Resource<Unit> = Resource.Success(Unit),
) : PlaylistRepository {
    val created = mutableListOf<String>()

    override suspend fun getBaseUrl() = "https://server/"
    override suspend fun getAllPlaylists(): Resource<List<Playlist>> = Resource.Success(playlists)
    override suspend fun getPlaylist(playlistId: String, start: Int, limit: Int): Resource<PlaylistPage> =
        Resource.Error(message = "unused")

    override suspend fun createPlaylist(name: String): Resource<Unit> {
        created += name
        return createResult
    }

    override suspend fun addToPlaylist(playlistId: String, item: PlaylistTarget) = Resource.Success(Unit)
    override suspend fun saveAsPlaylist(name: String, item: PlaylistTarget) = Resource.Success(Unit)
    override suspend fun renamePlaylist(playlist: Playlist, name: String) = Resource.Success(Unit)
    override suspend fun setPlaylistImage(playlist: Playlist, imageUri: String) = Resource.Success(Unit)
    override suspend fun removePlaylistImage(playlistId: String) = Resource.Success(Unit)
    override suspend fun togglePin(playlistId: String) = Resource.Success(Unit)
    override suspend fun deletePlaylist(playlistId: String) = Resource.Success(Unit)
    override suspend fun removeTrack(playlistId: String, trackHash: String, index: Int) = Resource.Success(Unit)
}
