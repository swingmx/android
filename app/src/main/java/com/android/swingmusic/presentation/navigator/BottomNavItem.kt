package com.android.swingmusic.presentation.navigator

import androidx.annotation.DrawableRes
import com.android.swingmusic.home.presentation.screen.destinations.HomeScreenDestination
import com.android.swingmusic.library.presentation.screen.destinations.FavouritesScreenDestination
import com.android.swingmusic.library.presentation.screen.destinations.LibraryScreenDestination
import com.android.swingmusic.search.presentation.screen.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.android.swingmusic.uicomponent.R as UiComponent

/**
 * A bottom bar tab. [animatedIcon] plays on each tap; tabs without one show [icon], or
 * [selectedIcon] while selected.
 */
sealed class BottomNavItem(
    val title: String,
    @param:DrawableRes val icon: Int,
    @param:DrawableRes val animatedIcon: Int? = null,
    @param:DrawableRes val selectedIcon: Int = icon,
    val destination: DestinationSpec<*>
) {
    data object Home : BottomNavItem(
        title = "Home",
        icon = UiComponent.drawable.ic_home,
        animatedIcon = UiComponent.drawable.avd_home,
        destination = HomeScreenDestination
    )

    data object Library : BottomNavItem(
        title = "Library",
        icon = UiComponent.drawable.ic_library,
        destination = LibraryScreenDestination
    )

    data object Favourites : BottomNavItem(
        title = "Favourites",
        icon = UiComponent.drawable.fav_not_filled,
        selectedIcon = UiComponent.drawable.fav_filled,
        destination = FavouritesScreenDestination
    )

    data object Search : BottomNavItem(
        title = "Search",
        icon = UiComponent.drawable.ic_search,
        animatedIcon = UiComponent.drawable.avd_search,
        destination = SearchScreenDestination
    )

    companion object {
        val all: List<BottomNavItem> by lazy { listOf(Home, Library, Favourites, Search) }
    }
}
