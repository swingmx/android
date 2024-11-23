package com.android.swingmusic.core.domain.util

import com.android.swingmusic.core.domain.model.TrackArtist

sealed class BottomSheetAction {
    data object GotoAlbum : BottomSheetAction()
    data class OpenArtistsDialog(val artists: List<TrackArtist>) : BottomSheetAction()
    data object PlayNext : BottomSheetAction()
    data object AddToQueue : BottomSheetAction()
    data object AddToPlaylist : BottomSheetAction()
    data class GotoFolder(val name: String, val path: String) : BottomSheetAction()
}
