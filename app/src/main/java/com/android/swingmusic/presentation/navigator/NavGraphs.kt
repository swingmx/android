package com.android.swingmusic.presentation.navigator

import com.android.swingmusic.album.presentation.screen.destinations.AlbumWithInfoScreenDestination
import com.android.swingmusic.album.presentation.screen.destinations.AllAlbumScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.AllArtistsScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistInfoScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ViewAllScreenOnArtistDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.home.presentation.destinations.HomeDestination
import com.android.swingmusic.player.presentation.screen.destinations.NowPlayingScreenDestination
import com.android.swingmusic.player.presentation.screen.destinations.QueueScreenDestination
import com.android.swingmusic.search.presentation.screen.destinations.SearchScreenDestination
import com.android.swingmusic.search.presentation.screen.destinations.ViewAllSearchResultsDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route

object NavGraphs {
    fun root(isUserLoggedIn: Boolean) = object : NavGraphSpec {
        override val route: String = "root"

        // TODO: Use Home instead of Folder as the startRoute
        override val startRoute: Route =
            if (isUserLoggedIn) FoldersAndTracksScreenDestination else LoginWithQrCodeDestination

        override val destinationsByRoute: Map<String, DestinationSpec<*>>
            get() {
                val preAuthDestSpec = listOf(
                    LoginWithQrCodeDestination,
                    LoginWithUsernameScreenDestination
                )

                val pastAuthDestSpec = listOf(
                    // shown on bottom nav
                    HomeDestination,
                    FoldersAndTracksScreenDestination,
                    AllAlbumScreenDestination,
                    AllArtistsScreenDestination,
                    SearchScreenDestination,

                    // inner destinations
                    NowPlayingScreenDestination,
                    QueueScreenDestination,
                    AlbumWithInfoScreenDestination,
                    ViewAllScreenOnArtistDestination,
                    ArtistInfoScreenDestination,
                    ViewAllSearchResultsDestination,
                )

                return (preAuthDestSpec + pastAuthDestSpec).associateBy { it.route }
            }

        override val nestedNavGraphs: List<NavGraphSpec> = emptyList()
    }
}
