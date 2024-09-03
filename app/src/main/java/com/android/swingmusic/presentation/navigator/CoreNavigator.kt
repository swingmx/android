package com.android.swingmusic.presentation.navigator

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.android.swingmusic.album.presentation.navigator.AlbumNavigator
import com.android.swingmusic.album.presentation.screen.destinations.AlbumWithInfoScreenDestination
import com.android.swingmusic.auth.presentation.navigation.AuthNavigator
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.player.presentation.navigator.PlayerNavigator
import com.android.swingmusic.player.presentation.screen.destinations.QueueScreenDestination
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
    AlbumNavigator,
        PlayerNavigator
{
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
    // REMEMBER: Bug -> Sometimes the app enters ANR state after this method is called
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

    /**----------------------------------- Album Navigator --------------------------------------*/
    override fun gotoAlbumWithInfo(albumHash: String) {
        val currentDestination = navController.currentDestination?.route
        val targetDestination = AlbumWithInfoScreenDestination(albumHash)

        if (currentDestination != targetDestination.route) {
            navController.navigate(targetDestination within navGraph)
        }
    }

    override fun navigateBack() {
        navController.navigateUp()
    }

    /**----------------------------------- Player Navigator -------------------------------------*/
    override fun gotoQueueScreen() {
        val currentDestination = navController.currentDestination?.route
        val targetDestination = QueueScreenDestination

        if (currentDestination != targetDestination.route) {
            navController.navigate(targetDestination within navGraph)
        }
    }
}
