package com.android.swingmusic.presentation.navigator

import androidx.annotation.DrawableRes
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.android.swingmusic.uicomponent.R as UiComponent

sealed class BottomNavItem(
    var title: String,
    @DrawableRes var icon: Int,
    var navGraph: NavGraphSpec?
) {
    data object Home : BottomNavItem(
        title = "Home",
        icon = UiComponent.drawable.ic_home,
        navGraph = null
    )

    data object Folder : BottomNavItem(
        title = "Folders",
        icon = UiComponent.drawable.folder_filled,
        navGraph = NavGraphs.folder
    )

    data object Album : BottomNavItem(
        title = "Albums",
        icon = UiComponent.drawable.ic_album,
        navGraph = null
    )

    data object Playlist : BottomNavItem(
        title = "Playlists",
        icon = UiComponent.drawable.play_list,
        navGraph = null
    )

    data object Artist : BottomNavItem(
        title = "Artists",
        icon = UiComponent.drawable.ic_artist,
        navGraph = NavGraphs.artist
    )
}
