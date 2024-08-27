package com.android.swingmusic.album.presentation.state

import com.android.swingmusic.core.data.util.Resource
import com.android.swingmusic.core.domain.model.AlbumWithInfo

data class AlbumWithInfoState(
    val albumHash: String? = null,
    val albumWithInfo: Resource<AlbumWithInfo> = Resource.Loading()
)
