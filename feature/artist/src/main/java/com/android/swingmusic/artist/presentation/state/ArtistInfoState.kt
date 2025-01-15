package com.android.swingmusic.artist.presentation.state

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.Artist
import com.android.swingmusic.core.domain.model.ArtistInfo

data class ArtistInfoState(
    val requiresReload: Boolean = true,
    val artistHashBackStack: List<String> = emptyList(),
    val infoResource: Resource<ArtistInfo> = Resource.Loading(),
    val similarArtistsResource: Resource<List<Artist>> = Resource.Loading()
)
