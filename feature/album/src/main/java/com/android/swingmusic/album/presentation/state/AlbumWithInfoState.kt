package com.android.swingmusic.album.presentation.state

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumInfo
import com.android.swingmusic.core.domain.model.Track

data class AlbumWithInfoState(
    val albumHash: String? = null,
    val reloadRequired: Boolean = true,
    val orderedTracks: List<Track> = emptyList(),
    val infoResource: Resource<AlbumInfoWithGroupedTracks> = Resource.Loading(),
)

data class AlbumInfoWithGroupedTracks(
    val albumInfo: AlbumInfo?,
    val groupedTracks: Map<Int, List<Track>>,
    val copyright: String?
)
