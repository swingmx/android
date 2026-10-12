package com.android.swingmusic.presentation.navigator

import androidx.navigation.NavController
import com.android.swingmusic.album.presentation.screen.destinations.AlbumWithInfoScreenDestination
import com.android.swingmusic.album.presentation.screen.destinations.AllAlbumScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.AllArtistsScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistInfoScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ViewAllScreenOnArtistDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.presentation.navigator.destinations.TabsRootScreenDestination
import com.android.swingmusic.profile.presentation.screen.destinations.LibraryManagementScreenDestination
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
        // Drop each tab's saved history so the next session starts fresh
        BottomNavItem.all.forEach { navController.clearBackStack(it.destination.route) }
    }

    override fun gotoHome() {
        // The tabs root opens Home, so every tab sits on the same base entry
        val targetDestination = TabsRootScreenDestination

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

    override fun gotoLibraryManagement() {
        navController.navigate(LibraryManagementScreenDestination) {
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

    override fun gotoAllAlbums() {
        navController.navigate(AllAlbumScreenDestination) {
            launchSingleTop = true
        }
    }

    override fun gotoAllArtists() {
        navController.navigate(AllArtistsScreenDestination) {
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
        // A new entry per artist, so similar artists stack and back returns to the previous one
        navController.navigate(ArtistInfoScreenDestination(artistHash = artistHash))
    }

    override fun gotoViewAllOnArtistScreen(
        artistHash: String,
        viewAllType: String,
        artistName: String,
        baseUrl: String
    ) {
        val targetDestination = ViewAllScreenOnArtistDestination(
            artistHash = artistHash,
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
