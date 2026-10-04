package com.android.swingmusic.presentation.navigator

import com.android.swingmusic.album.presentation.screen.destinations.AlbumWithInfoScreenDestination
import com.android.swingmusic.album.presentation.screen.destinations.AllAlbumScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.AllArtistsScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistInfoScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ViewAllScreenOnArtistDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.home.presentation.screen.destinations.HomeScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.LibraryScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.PairDeviceScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.StatsScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.SettingsScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.AccountScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.LyricsSettingsScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.StorageScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.AboutScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.ProfileScreenDestination
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

        override val startRoute: Route =
            if (isUserLoggedIn) HomeScreenDestination else LoginWithQrCodeDestination

        override val destinationsByRoute: Map<String, DestinationSpec<*>>
            get() {
                val preAuthDestSpec = listOf(
                    LoginWithQrCodeDestination,
                    LoginWithUsernameScreenDestination
                )

                val pastAuthDestSpec = listOf(
                    // shown on bottom nav
                    HomeScreenDestination,
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
                    ProfileScreenDestination,
                    LibraryScreenDestination,
                    PairDeviceScreenDestination,
                    StatsScreenDestination,
                    SettingsScreenDestination,
                    AccountScreenDestination,
                    LyricsSettingsScreenDestination,
                    StorageScreenDestination,
                    AboutScreenDestination,
                    FoldersAndTracksScreenDestination,
                )

                return (preAuthDestSpec + pastAuthDestSpec).associateBy { it.route }
            }

        override val nestedNavGraphs: List<NavGraphSpec> = emptyList()
    }
}
