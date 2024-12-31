package com.android.swingmusic.presentation.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.swingmusic.album.presentation.screen.destinations.AllAlbumScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistsScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ViewAllScreenDestination
import com.android.swingmusic.artist.presentation.viewmodel.ArtistInfoViewModel
import com.android.swingmusic.auth.data.workmanager.scheduleTokenRefreshWork
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksScreenDestination
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.player.presentation.screen.MiniPlayer
import com.android.swingmusic.player.presentation.screen.destinations.NowPlayingScreenDestination
import com.android.swingmusic.player.presentation.screen.destinations.QueueScreenDestination
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.presentation.navigator.*
import com.android.swingmusic.service.PlaybackService
import com.android.swingmusic.service.SessionTokenManager
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.NestedNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.NavGraphSpec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mediaControllerViewModel: MediaControllerViewModel by viewModels<MediaControllerViewModel>()
    private val authViewModel: AuthViewModel by viewModels<AuthViewModel>()
    private val artistInfoViewModel: ArtistInfoViewModel by viewModels<ArtistInfoViewModel>()
    private val foldersViewModel: FoldersViewModel by viewModels<FoldersViewModel>()

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun onStart() {
        super.onStart()

        val isUserLoggedIn by authViewModel.isUserLoggedIn()
        lifecycleScope.launch {
            authViewModel.isUserLoggedInFlow.collectLatest {
                if (it || isUserLoggedIn) {
                    initializeMediaController()
                }
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        scheduleTokenRefreshWork(applicationContext)

        setContent {
            val isUserLoggedIn by authViewModel.isUserLoggedIn()

            val playerState = mediaControllerViewModel.playerUiState.collectAsState()

            val navController = rememberNavController()
            val newBackStackEntry by navController.currentBackStackEntryAsState()
            val route = newBackStackEntry?.destination?.route

            val bottomNavItems: List<BottomNavItem> = listOf(
                // BottomNavItem.Home,
                BottomNavItem.Folder,
                BottomNavItem.Album,
                // BottomNavItem.Playlist,
                BottomNavItem.Artist,
            )

            // Show BottomBar Navigation if route is ONE of...
            val showBottomNav = route in listOf(
                "folder/${FoldersAndTracksScreenDestination.route}",
                "album/${AllAlbumScreenDestination.route}",
                "artist/${ArtistsScreenDestination.route}"
            )

            val bottomNavHeight by animateDpAsState(
                targetValue = if (showBottomNav) 80.dp else 0.dp,
                animationSpec = tween(durationMillis = 300),
                label = "Bottom Nav Height"
            )

            // Show Spacer below MiniPlayer if route NOT one of...
            val showSpacer = route !in listOf(
                "folder/${FoldersAndTracksScreenDestination.route}",
                "album/${AllAlbumScreenDestination.route}",
                "artist/${ArtistsScreenDestination.route}",
            )

            val spacerHeight by animateDpAsState(
                targetValue = if (showSpacer) 0.dp else 0.dp, // TODO: Fix Anim 24.dp <-> 12.dp
                animationSpec = tween(durationMillis = 300),
                label = "Spacer Height"
            )

            SwingMusicTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // show mini player on home, folder, artist and when btm nav is visible
                        // show btm nav on home, folder, ,

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Show mini player if route is NOT one of...
                            if ((route !in listOf<String>(
                                    "auth/${LoginWithQrCodeDestination.route}",
                                    "auth/${LoginWithUsernameScreenDestination.route}",
                                    "player/${NowPlayingScreenDestination.route}",
                                    "player/${QueueScreenDestination.route}",

                                    // Hide mini player in view all screen
                                    "player/${ViewAllScreenDestination.route}",
                                    "folder/${ViewAllScreenDestination.route}",
                                    "album/${ViewAllScreenDestination.route}",
                                    "artist/${ViewAllScreenDestination.route}",

                                    // Hide mini player in queue screen
                                    "folder/${QueueScreenDestination.route}",
                                    "album/${QueueScreenDestination.route}",
                                    "artist/${QueueScreenDestination.route}",
                                ))
                            ) {
                                MiniPlayer(
                                    mediaControllerViewModel = mediaControllerViewModel,
                                    onClickPlayerItem = {
                                        /*if (route !in listOf<String>(
                                                // make mini player un-clickable in view all screen
                                                "player/${ViewAllScreenDestination.route}",
                                                "folder/${ViewAllScreenDestination.route}",
                                                "album/${ViewAllScreenDestination.route}",
                                                "artist/${ViewAllScreenDestination.route}"
                                            )
                                        ) {
                                            navController.navigate(
                                                "player/${NowPlayingScreenDestination.route}",
                                                fun NavOptionsBuilder.() {
                                                    launchSingleTop = true
                                                    restoreState = false
                                                }
                                            )
                                        }*/

                                        navController.navigate(
                                            "player/${NowPlayingScreenDestination.route}",
                                            fun NavOptionsBuilder.() {
                                                launchSingleTop = true
                                                restoreState = false
                                            }
                                        )
                                    }
                                )

                                // Show spacer only when there's a playing track
                                playerState.value.nowPlayingTrack?.let {
                                    Box(
                                        modifier = Modifier
                                            .height(18.dp)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                                    )
                                }
                            }

                            val currentSelectedItem by navController.currentScreenAsState(
                                isUserLoggedIn
                            )

                            if (showBottomNav) {
                                NavigationBar(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                        item.navGraph,
                                                        fun NavOptionsBuilder.() {
                                                            launchSingleTop = true
                                                            restoreState = true
                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                saveState = true
                                                                inclusive = false
                                                            }
                                                        }
                                                    )

                                                    if (item.navGraph == NavGraphs.folder) {
                                                        foldersViewModel.onFolderUiEvent(
                                                            FolderUiEvent.OnClickNavPath(
                                                                folder = foldersViewModel.homeDir
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Surface {
                        SwingMusicAppNavigation(
                            isUserLoggedIn = isUserLoggedIn,
                            navController = navController,
                            authViewModel = authViewModel,
                            foldersViewModel = foldersViewModel,
                            artistInfoViewModel = artistInfoViewModel,
                            mediaControllerViewModel = mediaControllerViewModel
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

@OptIn(ExperimentalMaterialNavigationApi::class)
@ExperimentalAnimationApi
@Composable
internal fun SwingMusicAppNavigation(
    isUserLoggedIn: Boolean,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    foldersViewModel: FoldersViewModel,
    artistInfoViewModel: ArtistInfoViewModel,
    mediaControllerViewModel: MediaControllerViewModel
) {
    val animatedNavHostEngine = rememberAnimatedNavHostEngine(
        navHostContentAlignment = Alignment.TopCenter,
        rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING, // default `rootDefaultAnimations` means no animations
        defaultAnimationsForNestedNavGraph = mapOf(
            NavGraphs.auth to NestedNavGraphDefaultAnimations(
                enterTransition = {
                    scaleInEnterTransition()
                },
                exitTransition = {
                    scaleOutExitTransition()
                },
                popEnterTransition = {
                    scaleInPopEnterTransition()
                },
                popExitTransition = {
                    scaleOutPopExitTransition()
                }
            ),
            NavGraphs.home to NestedNavGraphDefaultAnimations(
                enterTransition = {
                    scaleInEnterTransition()
                },
                exitTransition = {
                    scaleOutExitTransition()
                },
                popEnterTransition = {
                    scaleInPopEnterTransition()
                },
                popExitTransition = {
                    scaleOutPopExitTransition()
                }
            ),
            NavGraphs.folder to NestedNavGraphDefaultAnimations(
                enterTransition = {
                    scaleInEnterTransition()
                },
                exitTransition = {
                    scaleOutExitTransition()
                },
                popEnterTransition = {
                    scaleInPopEnterTransition()
                },
                popExitTransition = {
                    scaleOutPopExitTransition()
                }
            ),
            NavGraphs.player to NestedNavGraphDefaultAnimations(
                enterTransition = {
                    scaleInEnterTransition()
                },
                exitTransition = {
                    scaleOutExitTransition()
                },
                popEnterTransition = {
                    scaleInPopEnterTransition()
                },
                popExitTransition = {
                    scaleOutPopExitTransition()
                }
            ),
            NavGraphs.album to NestedNavGraphDefaultAnimations(
                enterTransition = {
                    scaleInEnterTransition()
                },
                exitTransition = {
                    scaleOutExitTransition()
                },
                popEnterTransition = {
                    scaleInPopEnterTransition()
                },
                popExitTransition = {
                    scaleOutPopExitTransition()
                }
            ),
            NavGraphs.artist to NestedNavGraphDefaultAnimations(
                enterTransition = {
                    scaleInEnterTransition()
                },
                exitTransition = {
                    scaleOutExitTransition()
                },
                popEnterTransition = {
                    scaleInPopEnterTransition()
                },
                popExitTransition = {
                    scaleOutPopExitTransition()
                }
            )
        )
    )

    DestinationsNavHost(
        engine = animatedNavHostEngine,
        navController = navController,
        navGraph = NavGraphs.root(isUserLoggedIn),
        dependenciesContainerBuilder = {
            dependency(authViewModel)
            dependency(foldersViewModel)
            dependency(mediaControllerViewModel)
            dependency(artistInfoViewModel)
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
