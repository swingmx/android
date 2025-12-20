package com.android.swingmusic.presentation.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.swingmusic.album.presentation.screen.destinations.AlbumWithInfoScreenDestination
import com.android.swingmusic.album.presentation.screen.destinations.AllAlbumScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.AllArtistsScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ArtistInfoScreenDestination
import com.android.swingmusic.artist.presentation.screen.destinations.ViewAllScreenOnArtistDestination
import com.android.swingmusic.artist.presentation.viewmodel.ArtistInfoViewModel
import com.android.swingmusic.auth.data.workmanager.scheduleTokenRefreshWork
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.folder.presentation.event.FolderUiEvent
import com.android.swingmusic.folder.presentation.screen.destinations.FoldersAndTracksPaginatedScreenDestination
import com.android.swingmusic.folder.presentation.viewmodel.FoldersViewModel
import com.android.swingmusic.player.presentation.screen.MiniPlayer
import com.android.swingmusic.player.presentation.screen.destinations.NowPlayingScreenDestination
import com.android.swingmusic.player.presentation.screen.destinations.QueueScreenDestination
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.presentation.navigator.BottomNavItem
import com.android.swingmusic.presentation.navigator.CoreNavigator
import com.android.swingmusic.presentation.navigator.NavGraphs
import com.android.swingmusic.presentation.navigator.scaleInEnterTransition
import com.android.swingmusic.presentation.navigator.scaleInPopEnterTransition
import com.android.swingmusic.presentation.navigator.scaleOutExitTransition
import com.android.swingmusic.presentation.navigator.scaleOutPopExitTransition
import com.android.swingmusic.search.presentation.event.SearchUiEvent
import com.android.swingmusic.search.presentation.screen.destinations.SearchScreenDestination
import com.android.swingmusic.search.presentation.screen.destinations.ViewAllSearchResultsDestination
import com.android.swingmusic.search.presentation.viewmodel.SearchViewModel
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
import com.ramcosta.composedestinations.utils.destination
import android.Manifest
import android.os.Build
import com.android.swingmusic.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mediaControllerViewModel: MediaControllerViewModel by viewModels<MediaControllerViewModel>()
    private val authViewModel: AuthViewModel by viewModels<AuthViewModel>()
    private val foldersViewModel: FoldersViewModel by viewModels<FoldersViewModel>()
    private val artistInfoViewModel: ArtistInfoViewModel by viewModels<ArtistInfoViewModel>()
    private val searchViewModel: SearchViewModel by viewModels<SearchViewModel>()

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            authViewModel.isUserLoggedIn.collectLatest {
                if (it == true) {
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

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        scheduleTokenRefreshWork(applicationContext)

        setContent {
            val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

            val playerState = mediaControllerViewModel.playerUiState.collectAsState()

            val navController = rememberNavController()
            val newBackStackEntry by navController.currentBackStackEntryAsState()
            val route = newBackStackEntry?.destination?.route

            val hideForDestination = listOf(
                LoginWithUsernameScreenDestination,
                LoginWithQrCodeDestination,
                NowPlayingScreenDestination,
                QueueScreenDestination
            )

            val showBottomNav =
                route != null && newBackStackEntry?.destination() !in hideForDestination

            val bottomNavItems: List<BottomNavItem> = listOf(
                // BottomNavItem.Home,
                BottomNavItem.Folder,
                BottomNavItem.Album,
                // BottomNavItem.Playlist,
                BottomNavItem.Artist,
                BottomNavItem.Search,
            )

            // Map of BottomNavItem to their route prefixes
            val bottomNavRoutePrefixes = mapOf(
                // BottomNavItem.Home to listOf(HomeDestination.route),
                BottomNavItem.Folder to listOf(FoldersAndTracksPaginatedScreenDestination.route),
                BottomNavItem.Album to listOf(
                    AllAlbumScreenDestination.route,
                    AlbumWithInfoScreenDestination.route
                ),
                BottomNavItem.Artist to listOf(
                    AllArtistsScreenDestination.route,
                    ArtistInfoScreenDestination.route,
                    ViewAllScreenOnArtistDestination.route
                ),
                BottomNavItem.Search to listOf(
                    SearchScreenDestination.route,
                    ViewAllSearchResultsDestination.route
                )
            )


            SwingMusicTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Show mini player whenever bottom nav is visible
                            if (showBottomNav) {
                                MiniPlayer(
                                    mediaControllerViewModel = mediaControllerViewModel,
                                    onClickPlayerItem = {
                                        navController.navigate(
                                            NowPlayingScreenDestination.route
                                        ) {
                                            launchSingleTop = true
                                            restoreState = false
                                        }
                                    }
                                )

                                // Show spacer only when there's a playing track
                                playerState.value.nowPlayingTrack?.let {
                                    Box(
                                        modifier = Modifier
                                            .height(12.dp)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.inverseOnSurface)
                                    )
                                }
                            }

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
                                                selected = navController.currentDestination?.route?.let { route ->
                                                    bottomNavRoutePrefixes[item]?.any { prefix ->
                                                        route.startsWith(prefix)
                                                    } == true
                                                } == true,
                                                alwaysShowLabel = false,
                                                label = { Text(text = item.title) },
                                                onClick = {
                                                    // Whatever you do, DON'T TOUCH this
                                                    if (navController.currentDestination?.route != item.destination.route) {
                                                        navController.navigate(item.destination.route) {
                                                            launchSingleTop = true
                                                            restoreState = false

                                                            popUpTo(navController.graph.startDestinationId) {
                                                                saveState = false
                                                                inclusive = false
                                                            }
                                                        }
                                                    }

                                                    // refresh folders starting from $home
                                                    if (item.destination.route == FoldersAndTracksPaginatedScreenDestination.route) {
                                                        foldersViewModel.onFolderUiEvent(
                                                            FolderUiEvent.OnClickNavPath(
                                                                folder = foldersViewModel.homeDir
                                                            )
                                                        )
                                                    }

                                                    // refresh Search screen
                                                    if (item.destination.route == SearchScreenDestination.route) {
                                                        searchViewModel.onSearchUiEvent(
                                                            SearchUiEvent.OnClearSearchStates
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
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = isUserLoggedIn == null,
                            enter = fadeIn(animationSpec = tween(durationMillis = 100))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        isUserLoggedIn?.let { value ->
                            SwingMusicAppNavigation(
                                isUserLoggedIn = value,
                                navController = navController,
                                authViewModel = authViewModel,
                                mediaControllerViewModel = mediaControllerViewModel,
                                foldersViewModel = foldersViewModel,
                                artistInfoViewModel = artistInfoViewModel,
                                searchViewModel = searchViewModel
                            )
                        }
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
    mediaControllerViewModel: MediaControllerViewModel,
    foldersViewModel: FoldersViewModel,
    artistInfoViewModel: ArtistInfoViewModel,
    searchViewModel: SearchViewModel
) {
    val navGraph = remember(isUserLoggedIn) { NavGraphs.root(isUserLoggedIn) }

    val animatedNavHostEngine = rememberAnimatedNavHostEngine(
        navHostContentAlignment = Alignment.TopCenter,
        rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING,
        defaultAnimationsForNestedNavGraph = mapOf(
            NavGraphs.root(isUserLoggedIn) to NestedNavGraphDefaultAnimations(
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
        navGraph = navGraph,
        dependenciesContainerBuilder = {
            dependency(
                CoreNavigator(navController = navController)
            )
            dependency(authViewModel)
            dependency(foldersViewModel)
            dependency(mediaControllerViewModel)
            dependency(artistInfoViewModel)
            dependency(searchViewModel)
        }
    )
}
