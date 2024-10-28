package com.android.swingmusic.presentation.navigator

import com.android.swingmusic.album.presentation.screen.destinations.AlbumWithInfoScreenDestination
import com.android.swingmusic.album.presentation.screen.destinations.AllAlbumScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistInfoScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistsScreenDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.home.presentation.destinations.HomeDestination
import com.android.swingmusic.player.presentation.screen.destinations.NowPlayingScreenDestination
import com.android.swingmusic.player.presentation.screen.destinations.QueueScreenDestination
import com.ramcosta.composedestinations.dynamic.routedIn
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route

object NavGraphs {
    val auth = object : NavGraphSpec {
        override val route: String = "auth"

        override val startRoute: Route = LoginWithQrCodeDestination routedIn this

        override val destinationsByRoute: Map<String, DestinationSpec<*>> =
            listOf<DestinationSpec<*>>(
                LoginWithQrCodeDestination,
                LoginWithUsernameScreenDestination
            ).routedIn(this).associateBy { it.route }
    }

    val home = object : NavGraphSpec {
        override val route: String = "home"

        override val startRoute: Route = HomeDestination routedIn this

        override val destinationsByRoute: Map<String, DestinationSpec<*>> =
            listOf<DestinationSpec<*>>(
                HomeDestination
            ).routedIn(this).associateBy { it.route }
    }

    val folder = object : NavGraphSpec {
        override val route: String = "folder"

        override val startRoute: Route = FoldersAndTracksScreenDestination routedIn this

        override val destinationsByRoute: Map<String, DestinationSpec<*>> =
            listOf<DestinationSpec<*>>(
                FoldersAndTracksScreenDestination,
                AlbumWithInfoScreenDestination,
                ArtistInfoScreenDestination
            ).routedIn(this).associateBy { it.route }
    }

    val player = object : NavGraphSpec {
        override val route: String = "player"

        override val startRoute: Route = NowPlayingScreenDestination routedIn this

        override val destinationsByRoute: Map<String, DestinationSpec<*>> =
            listOf<DestinationSpec<*>>(
                NowPlayingScreenDestination,
                QueueScreenDestination
            ).routedIn(this).associateBy { it.route }
    }

    val artist = object : NavGraphSpec {
        override val route: String = "artist"

        override val startRoute: Route = ArtistsScreenDestination routedIn this

        override val destinationsByRoute: Map<String, DestinationSpec<*>> =
            listOf<DestinationSpec<*>>(
                ArtistsScreenDestination,
                ArtistInfoScreenDestination
            ).routedIn(this).associateBy { it.route }
    }

    val album = object : NavGraphSpec {
        override val route: String = "album"

        override val startRoute: Route = AllAlbumScreenDestination routedIn this

        override val destinationsByRoute: Map<String, DestinationSpec<*>> =
            listOf<DestinationSpec<*>>(
                AllAlbumScreenDestination,
                AlbumWithInfoScreenDestination,
                ArtistInfoScreenDestination
            ).routedIn(this).associateBy { it.route }
    }

    fun root(isUserLoggedIn: Boolean) = object : NavGraphSpec {
        override val route: String = "root"

        // TODO:......Use home as the start route instead of folder
        override val startRoute: Route = if (isUserLoggedIn) folder else auth

        override val destinationsByRoute: Map<String, DestinationSpec<*>> = emptyMap()

        override val nestedNavGraphs: List<NavGraphSpec> = listOf(
            auth,
            // home,
            player,
            // playlist,
            album,
            folder,
            artist
        )
    }
}
