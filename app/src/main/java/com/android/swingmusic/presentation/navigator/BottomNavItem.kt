package com.android.swingmusic.presentation.navigator

import androidx.annotation.DrawableRes
import com.android.swingmusic.album.presentation.screen.destinations.AllAlbumScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.AllArtistsScreenDestination
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.search.presentation.screen.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.android.swingmusic.uicomponent.R as UiComponent

sealed class BottomNavItem(
    var title: String,
    @param:DrawableRes var icon: Int,
    @param:DrawableRes var animatedIcon: Int,
    var destination: DestinationSpec<*>
) {
    data object Folder : BottomNavItem(
        title = "Folders",
        icon = UiComponent.drawable.folder_filled,
        animatedIcon = UiComponent.drawable.avd_folder,
        destination = FoldersAndTracksScreenDestination
    )

    data object Album : BottomNavItem(
        title = "Albums",
        icon = UiComponent.drawable.ic_album,
        animatedIcon = UiComponent.drawable.avd_album,
        destination = AllAlbumScreenDestination
    )

    data object Artist : BottomNavItem(
        title = "Artists",
        icon = UiComponent.drawable.ic_artist,
        animatedIcon = UiComponent.drawable.avd_artist,
        destination = AllArtistsScreenDestination
    )

    data object Search : BottomNavItem(
        title = "Search",
        icon = UiComponent.drawable.ic_search,
        animatedIcon = UiComponent.drawable.avd_search,
        destination = SearchScreenDestination
    )
}
