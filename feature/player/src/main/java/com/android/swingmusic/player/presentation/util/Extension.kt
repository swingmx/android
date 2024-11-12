package com.android.swingmusic.player.presentation.util

import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.util.QueueSource

fun QueueSource.navigateToSource(navigator: CommonNavigator) {
    return when (this) {
        is QueueSource.ALBUM -> navigator.gotoAlbumWithInfo(this.albumHash)
        is QueueSource.ARTIST -> navigator.gotoArtistInfo(this.artistHash)
        //   is QueueSource.FOLDER -> navigator.gotoSourceFolder(this.path)
        // is QueueSource.PLAYLIST -> ...
        // is QueueSource.QUERY -> ...
        else -> {}
    }
}
