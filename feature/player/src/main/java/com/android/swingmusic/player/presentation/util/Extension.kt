package com.android.swingmusic.player.presentation.util

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.util.QueueSource
import kotlin.math.absoluteValue

fun QueueSource.navigateToSource(navigator: CommonNavigator) {
    return when (this) {
        is QueueSource.ALBUM -> navigator.gotoAlbumWithInfo(this.albumHash)
        is QueueSource.ARTIST -> navigator.gotoArtistInfo(this.artistHash)
        is QueueSource.FOLDER -> navigator.gotoSourceFolder(this.name, this.path)
        // is QueueSource.PLAYLIST -> ...
        // is QueueSource.QUERY -> ...
        else -> {}
    }
}

// extension method for current page offset
fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    return (currentPage - page) + currentPageOffsetFraction
}
