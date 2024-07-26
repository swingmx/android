package com.android.swingmusic.presentation.activity

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistsScreenDestination
import com.android.swingmusic.auth.data.workmanager.scheduleTokenRefreshWork
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.home.presentation.destinations.HomeDestination
import com.android.swingmusic.player.presentation.screen.MiniPlayer
import com.android.swingmusic.player.presentation.screen.NowPlayingScreen
import com.android.swingmusic.player.presentation.screen.QueueScreen
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.presentation.navigator.BottomNavItem
import com.android.swingmusic.presentation.navigator.CoreNavigator
import com.android.swingmusic.presentation.navigator.NavGraphs
import com.android.swingmusic.service.PlaybackService
import com.android.swingmusic.service.SessionTokenManager
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.spec.NavGraphSpec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mediaControllerViewModel: MediaControllerViewModel by viewModels<MediaControllerViewModel>()
    private val authViewModel: AuthViewModel by viewModels<AuthViewModel>()

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var showNowPlayingOnStart = false

    /** Show NowPlaying since intents.extras is only non-null when app
     * is initiated from the media Notification.
     * Refer to pending intent defined in [PlaybackService] */
    override fun onStart() {
        super.onStart()
        showNowPlayingOnStart = intent.getBooleanExtra("SHOW_NOW_PLAYING", false)

        val isUserLoggedIn by authViewModel.isUserLoggedIn()
        lifecycleScope.launch {
            authViewModel.isUserLoggedInFlow.collectLatest {
                if (it || isUserLoggedIn) {
                    initializeMediaController()
                }
            }
        }
    }

    @OptIn(
        ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleTokenRefreshWork(applicationContext)

        setContent {
            val isUserLoggedIn by authViewModel.isUserLoggedIn()
            val coroutineScope = rememberCoroutineScope()
            var showNowPlayingBottomSheet by rememberSaveable { mutableStateOf(showNowPlayingOnStart) }
            val nowPlayingBottomSheetState = rememberModalBottomSheetState(true)

            var showQueueBottomSheet by rememberSaveable { mutableStateOf(false) }
            val queueBottomSheetState = rememberModalBottomSheetState(true)

            // val navController = rememberAnimatedNavController()
            val navController = rememberNavController()
            val newBackStackEntry by navController.currentBackStackEntryAsState()
            val route = newBackStackEntry?.destination?.route

            val bottomNavItems: List<BottomNavItem> = listOf(
                // BottomNavItem.Home,
                BottomNavItem.Folder,
                // BottomNavItem.Album,
                // BottomNavItem.Playlist,
                // BottomNavItem.Artist,
            )

            SwingMusicTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // show mini player on home, folder, artist and when btm nav is visible
                        // show btm nav on home, folder, ,

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Show mini player if route is NOT one of...
                            if ((route in listOf<String>(
                                    "auth/${LoginWithQrCodeDestination.route}",
                                    "auth/${LoginWithUsernameScreenDestination.route}"
                                )).not()
                            ) {
                                MiniPlayer(
                                    mediaControllerViewModel = mediaControllerViewModel,
                                    onClickPlayerItem = {
                                        showNowPlayingBottomSheet = true
                                    }
                                )
                            }

                            // Show BottomBar Navigation if route is one of...
                            if (route in listOf(
                                    "home/${HomeDestination.route}",
                                    "folder/${FoldersAndTracksScreenDestination.route}",
                                    // "album/${AlbumsScreenDestination.route}",
                                    // "playlist/${PlaylistsScreenDestination.route}",
                                    "artist/${ArtistsScreenDestination.route}",
                                )
                            ) {
                                val currentSelectedItem by navController.currentScreenAsState(
                                    isUserLoggedIn
                                )
                                // REMEMBER: Return NavigationBar when at least 2 items are ready

                                /* NavigationBar(
                                     modifier = Modifier,
                                     containerColor = MaterialTheme.colorScheme.inverseOnSurface
                                 ) {
                                     Row(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.Center
                                     ) {
                                         bottomNavItems.forEach { item ->
                                             NavigationBarItem(
                                                 icon = {
                                                     Icon(
                                                         painter = painterResource(id = item.icon),
                                                         contentDescription = null
                                                     )
                                                 },
                                                 selected = currentSelectedItem == item.navGraph,
                                                 alwaysShowLabel = false,
                                                 label = { Text(text = item.title) },
                                                 onClick = {
                                                     navController.navigate(
                                                         item.navGraph!!,
                                                         fun NavOptionsBuilder.() {
                                                             launchSingleTop = true
                                                             restoreState = true

                                                             popUpTo(navController.graph.findStartDestination().id) {
                                                                 saveState = true
                                                                 inclusive = false
                                                             }
                                                         }
                                                     )
                                                 }
                                             )
                                         }
                                     }
                                 }*/
                            }
                        }
                    }
                ) {
                    Surface(modifier = Modifier.padding(it)) {
                        SwingMusicAppNavigation(
                            isUserLoggedIn = isUserLoggedIn,
                            navController = navController,
                            authViewModel = authViewModel,
                            mediaControllerViewModel = mediaControllerViewModel
                        )
                    }
                }

                /**--------------------------- Player BottomSheet ------------------------------- */
                if (showNowPlayingBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showNowPlayingBottomSheet = false },
                        sheetState = nowPlayingBottomSheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        dragHandle = {}
                    ) {
                        NowPlayingScreen(
                            mediaControllerViewModel = mediaControllerViewModel,
                            onClickOpenQueue = { showQueueBottomSheet = true }
                        )
                    }
                }

                /**--------------------------- Queue BottomSheet -------------------------------- */
                if (showQueueBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showQueueBottomSheet = false },
                        sheetState = queueBottomSheetState,
                        dragHandle = {}
                    ) {
                        QueueScreen(
                            mediaControllerViewModel = mediaControllerViewModel,
                            onClickBack = {
                                coroutineScope.launch { queueBottomSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!queueBottomSheetState.isVisible) {
                                            showQueueBottomSheet = false
                                        }
                                    }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun initializeMediaController() {
        if (
            mediaControllerViewModel.getMediaController() == null ||
            (this::controllerFuture.isInitialized).not()
        ) {
            val sessionToken = SessionTokenManager.sessionToken
            if (sessionToken != null) {
                // Use the existing session token to build the MediaController
                controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
                controllerFuture.addListener(
                    {
                        val mediaController = controllerFuture.get()
                        mediaControllerViewModel.reconnectMediaController(mediaController)
                    }, MoreExecutors.directExecutor()
                )
            } else {
                // Create a new session token if no existing token is found
                val newSessionToken =
                    SessionToken(this, ComponentName(this, PlaybackService::class.java))

                controllerFuture = MediaController.Builder(this, newSessionToken).buildAsync()
                controllerFuture.addListener(
                    {
                        val mediaController = controllerFuture.get()
                        mediaControllerViewModel.setMediaController(mediaController)
                    }, MoreExecutors.directExecutor()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::controllerFuture.isInitialized) {
            mediaControllerViewModel.releaseMediaController(controllerFuture)
        }
    }
}

@ExperimentalAnimationApi
@Composable
internal fun SwingMusicAppNavigation(
    isUserLoggedIn: Boolean,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mediaControllerViewModel: MediaControllerViewModel
) {
    val navHostEngineNoAnim = rememberNavHostEngine()
    /* val bottomSheetNavigator = rememberBottomSheetNavigator()
     navController.navigatorProvider.addNavigator(bottomSheetNavigator) */

    DestinationsNavHost(
        engine = navHostEngineNoAnim,
        navController = navController,
        navGraph = NavGraphs.root(isUserLoggedIn),
        dependenciesContainerBuilder = {
            dependency(authViewModel)
            dependency(mediaControllerViewModel)
            dependency(
                CoreNavigator(
                    navGraph = navBackStackEntry.destination.getNavGraph(isUserLoggedIn),
                    navController = navController,
                    authViewModel = authViewModel,
                    mediaControllerViewModel = mediaControllerViewModel
                )
            )
        }
    )
}

@Stable
@Composable
private fun NavController.currentScreenAsState(isUserLoggedIn: Boolean): State<NavGraphSpec> {
    // ---------------------Todo: Change this to NavGraphs.home
    val selectedItem: MutableState<NavGraphSpec> = remember { mutableStateOf(NavGraphs.folder) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            selectedItem.value = destination.getNavGraph(isUserLoggedIn)
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

private fun NavDestination.getNavGraph(isUserLoggedIn: Boolean): NavGraphSpec {
    hierarchy.forEach { destination ->
        NavGraphs.root(isUserLoggedIn).nestedNavGraphs.forEach { navGraph ->
            if (destination.route == navGraph.route) {
                return navGraph
            }
        }
    }

    throw RuntimeException("Unknown nav graph for destination $route")
}
