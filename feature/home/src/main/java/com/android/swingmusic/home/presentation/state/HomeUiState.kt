package com.android.swingmusic.home.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.core.domain.model.HomeItem
import com.android.swingmusic.core.domain.model.HomeSection

@Immutable
internal data class HomeUiState(
    val baseUrl: String = "",
    val avatarPath: String? = null,
    val isRefreshing: Boolean = false,

    val isLoadingSections: Boolean = true,
    val errorLoadingSections: String? = null,
    val sections: List<HomeSection> = emptyList(),

    val loadingItemKey: String? = null,
)

internal val HomeItem.key: String
    get() = when (this) {
        is HomeItem.AlbumItem -> "album:${album.albumHash}"
        is HomeItem.ArtistItem -> "artist:${artist.artistHash}"
        is HomeItem.TrackItem -> "track:${track.trackHash}"
        is HomeItem.PlaylistItem -> "playlist:${playlist.id}"
        is HomeItem.MixItem -> "mix:${mix.id}"
    }
