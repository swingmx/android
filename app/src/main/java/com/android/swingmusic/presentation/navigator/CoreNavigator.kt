package com.android.swingmusic.presentation.navigator

import androidx.navigation.NavController
import com.android.swingmusic.album.presentation.screen.destinations.AlbumWithInfoScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistInfoScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ViewAllScreenOnArtistDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.home.presentation.screen.destinations.HomeScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.LibraryScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.PairDeviceScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.AvatarCropScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.StatsScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.SettingsScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.AccountScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.LyricsSettingsScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.StorageScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.AboutScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.ProfileScreenDestination
import com.android.swingmusic.player.presentation.screen.destinations.QueueScreenDestination
import com.android.swingmusic.search.presentation.screen.destinations.ViewAllSearchResultsDestination
import com.ramcosta.composedestinations.navigation.navigate

class CoreNavigator(
    private val navController: NavController
) : CommonNavigator {

    /**----------------------------------- Auth Navigator ----------------------------------------*/
    override fun gotoLoginWithUsername() {
        val targetDestination = LoginWithUsernameScreenDestination

        navController.navigate(targetDestination) {
            launchSingleTop = true
            restoreState = false

            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
                saveState = false
            }
        }
    }

    override fun gotoLoginWithQrCode() {
        val targetDestination = LoginWithQrCodeDestination

        navController.navigate(targetDestination) {
            launchSingleTop = true
            restoreState = false

            popUpTo(navController.graph.id) {
                inclusive = true
                saveState = false
            }
        }
    }

    override fun gotoHome() {
        val targetDestination = HomeScreenDestination

        navController.navigate(targetDestination) {
            launchSingleTop = true
            restoreState = false

            popUpTo(navController.graph.id) {
                inclusive = true
                saveState = false
            }
        }
    }

    override fun gotoProfile() {
        navController.navigate(ProfileScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoStats() {
        navController.navigate(StatsScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoLibrary() {
        navController.navigate(LibraryScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoAvatarCrop(imageUri: String) {
        navController.navigate(AvatarCropScreenDestination(imageUri)) {
            launchSingleTop = true
        }
    }

    override fun gotoPairDevice() {
        navController.navigate(PairDeviceScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoFolders() {
        navController.navigate(FoldersAndTracksScreenDestination()) {
            launchSingleTop = true
        }
    }

    override fun gotoSettings() {
        navController.navigate(SettingsScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoAccountSettings() {
        navController.navigate(AccountScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoLyricsSettings() {
        navController.navigate(LyricsSettingsScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoStorageSettings() {
        navController.navigate(StorageScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoAbout() {
        navController.navigate(AboutScreenDestination) {
            launchSingleTop = true
        }
    }

    /**----------------------------------- Album Navigator --------------------------------------*/
    override fun gotoAlbumWithInfo(albumHash: String) {
        val targetDestination = AlbumWithInfoScreenDestination(albumHash)

        navController.navigate(targetDestination) {
            launchSingleTop = true
        }
    }

    override fun navigateBack() {
        navController.navigateUp()
    }

    /**----------------------------------- Player Navigator -------------------------------------*/
    override fun gotoQueueScreen() {
        val targetDestination = QueueScreenDestination

        navController.navigate(targetDestination) {
            launchSingleTop = true
        }

    }

    override fun gotoArtistInfo(artistHash: String) {
        val targetDestination = ArtistInfoScreenDestination(
            artistHash = artistHash,
            loadNewArtist = true
        )

        navController.navigate(targetDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoViewAllOnArtistScreen(
        viewAllType: String,
        artistName: String,
        baseUrl: String
    ) {
        val targetDestination = ViewAllScreenOnArtistDestination(
            viewAllType = viewAllType,
            artistName = artistName,
            baseUrl = baseUrl
        )

        navController.navigate(targetDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoViewAllSearchResultsScreen(
        viewAllType: String,
        searchParams: String
    ) {
        val targetDestination = ViewAllSearchResultsDestination(
            searchParams = searchParams,
            viewAllType = viewAllType
        )

        navController.navigate(targetDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoSourceFolder(name: String, path: String) {
        val targetDestination = FoldersAndTracksScreenDestination(name, path)
        navController.navigate(targetDestination) {
            launchSingleTop = true
        }
    }
}
