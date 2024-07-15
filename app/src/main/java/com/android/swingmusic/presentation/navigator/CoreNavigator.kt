package com.android.swingmusic.presentation.navigator

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.android.swingmusic.artist.presentation.navigator.ArtistNavigator
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistsScreenDestination
import com.android.swingmusic.auth.presentation.navigation.AuthNavigator
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.player.presentation.navigator.PlayerNavigator
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.ramcosta.composedestinations.dynamic.within
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.NavGraphSpec

class CoreNavigator(
    private val navGraph: NavGraphSpec,
    private val navController: NavController,
    private val mediaControllerViewModel: MediaControllerViewModel,
    private val authViewModel: AuthViewModel
) : AuthNavigator,
    ArtistNavigator,
    PlayerNavigator {

    /**----------------------------------- Auth Navigator ----------------------------------------*/
    override fun gotoLoginWithUsername() {
        val currentDestination = navController.currentDestination?.route
        val targetDestination = LoginWithUsernameScreenDestination

        if (currentDestination != targetDestination.route) {
            navController.navigate(targetDestination within navGraph,
                fun NavOptionsBuilder.() {
                    launchSingleTop = true
                    restoreState = false

                    popUpTo(navController.graph.startDestinationId) {
                        saveState = false
                        inclusive = true
                    }
                }
            )
        }
    }

    override fun gotoLoginWithQrCode() {
        val currentDestination = navController.currentDestination?.route
        val targetDestination = LoginWithQrCodeDestination

        if (currentDestination != targetDestination.route) {
            navController.navigate(targetDestination within navGraph,
                fun NavOptionsBuilder.() {
                    launchSingleTop = true
                    restoreState = false

                    popUpTo(navController.graph.startDestinationId) {
                        saveState = false
                        inclusive = true
                    }
                }
            )
        }
    }

    override fun gotoHomeNavGraph() {
        mediaControllerViewModel.refreshBaseUrl()
        authViewModel.updateIsUserLoggedInFlow() // flow collected in the main activity

        val currentDestination = navController.currentDestination?.route
        val homeNavGraph = NavGraphs.home

        if (currentDestination != homeNavGraph.route) {
            navController.navigate(
                homeNavGraph,
                fun NavOptionsBuilder.() {
                    launchSingleTop = true
                    restoreState = true

                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                        inclusive = true
                    }
                }
            )
        }
    }

    // Todo: Remove this after adding home content
    override fun gotoFolderNavGraph() {
        mediaControllerViewModel.refreshBaseUrl()
        authViewModel.updateIsUserLoggedInFlow() // flow collected in the main activity

        val currentDestination = navController.currentDestination?.route
        val folderNavGraph = NavGraphs.folder

        if (currentDestination != folderNavGraph.route) {
            navController.navigate(
                folderNavGraph,
                fun NavOptionsBuilder.() {
                    launchSingleTop = true
                    restoreState = true

                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                        inclusive = true
                    }
                }
            )
        }
    }

    /**----------------------------------- Artist Navigator --------------------------------------*/
    override fun gotoArtists() {
        val currentDestination = navController.currentDestination?.route
        val targetDestination = ArtistsScreenDestination

        if (currentDestination != targetDestination.route) {
            navController.navigate(targetDestination within navGraph,
                fun NavOptionsBuilder.() {
                    launchSingleTop = true
                    restoreState = true

                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                        inclusive = false
                    }
                }
            )
        }
    }

    override fun gotoArtistDetails() {
        TODO("Not yet implemented")
    }

    override fun navBackToArtists() {
        navController.navigateUp()
    }

    /**----------------------------------- Player Navigator --------------------------------------*/

}
