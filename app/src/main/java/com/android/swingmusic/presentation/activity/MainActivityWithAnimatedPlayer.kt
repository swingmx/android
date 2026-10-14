package com.android.swingmusic.presentation.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.android.swingmusic.auth.data.workmanager.scheduleTokenRefreshWork
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithQrCodeDestination
import com.android.swingmusic.auth.presentation.screen.destinations.LoginWithUsernameScreenDestination
import com.android.swingmusic.auth.presentation.viewmodel.AuthViewModel
import com.android.swingmusic.home.presentation.screen.destinations.HomeScreenDestination
import com.android.swingmusic.player.presentation.screen.AnimatedPlayerSheet
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.presentation.navigator.BottomNavItem
import com.android.swingmusic.presentation.navigator.destinations.TabsRootScreenDestination
import com.android.swingmusic.presentation.navigator.CoreNavigator
import com.android.swingmusic.presentation.navigator.NavGraphs
import com.android.swingmusic.presentation.navigator.scaleInEnterTransition
import com.android.swingmusic.presentation.navigator.scaleInPopEnterTransition
import com.android.swingmusic.presentation.navigator.scaleOutExitTransition
import com.android.swingmusic.presentation.navigator.scaleOutPopExitTransition
import com.android.swingmusic.search.presentation.event.SearchUiEvent
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
import android.Manifest
import android.os.Build
import androidx.activity.enableEdgeToEdge
import com.android.swingmusic.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * MainActivityWithAnimatedPlayer - Alternative MainActivity with animated bottom sheet player.
 *
 * This activity replaces the traditional MiniPlayer + navigation-based NowPlaying approach
 * with a continuous drag-animated bottom sheet that transforms from mini player to full player.
 *
 * Key differences from MainActivity:
 * - Uses AnimatedPlayerSheet instead of MiniPlayer
 * - Navigation bar animates based on sheet expansion progress
 * - No navigation to NowPlayingScreen - player is always a sheet overlay
 *
 * To use this activity:
 * 1. Update AndroidManifest.xml to set this as the launcher activity
 * 2. Or change the activity reference in your launch intent
 */
@AndroidEntryPoint
class MainActivityWithAnimatedPlayer : ComponentActivity() {
    private val mediaControllerViewModel: MediaControllerViewModel by viewModels<MediaControllerViewModel>()
    private val authViewModel: AuthViewModel by viewModels<AuthViewModel>()
    private val searchViewModel: SearchViewModel by viewModels<SearchViewModel>()

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            authViewModel.isUserLoggedIn.collectLatest {
                if (it == true) {
                    scheduleTokenRefreshWork(applicationContext)
                    mediaControllerViewModel.refreshBaseUrl()
                    initializeMediaController()
                }
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalAnimationApi::class, ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        // enableEdgeToEdge()

        setContent {
            val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

            val playerState = mediaControllerViewModel.playerUiState.collectAsState()

            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                authViewModel.sessionEnded.collect { reason ->
                    mediaControllerViewModel.endSession()
                    imageLoader.memoryCache?.clear()
                    imageLoader.diskCache?.clear()
                    authViewModel.onSessionEnded(reason)
                    CoreNavigator(navController).gotoLoginWithQrCode()
                }
            }

            val newBackStackEntry by navController.currentBackStackEntryAsState()
            val route = newBackStackEntry?.destination?.route

            // Destinations where bottom nav should be hidden (check by route string)
            val hideForRoutes = listOf(
                LoginWithUsernameScreenDestination.route,
                LoginWithQrCodeDestination.route
            )

            val showBottomNav = route != null && hideForRoutes.none { route.startsWith(it) }

            val bottomNavItems = BottomNavItem.all

            // The tab whose history is showing. Kept as state rather than read from the route,
            // since screens like an album page live in whichever tab opened them.
            var currentTab by rememberSaveable { mutableStateOf(BottomNavItem.Home.title) }

            fun selectTab(item: BottomNavItem) {
                if (item.title == currentTab) {
                    // Reselecting returns the tab to its first screen
                    val popped = navController.popBackStack(item.destination.route, inclusive = false)
                    if (!popped) navController.navigate(item.destination.route) { launchSingleTop = true }
                    if (item == BottomNavItem.Search) {
                        searchViewModel.onSearchUiEvent(SearchUiEvent.OnClearSearchStates)
                    }
                    return
                }
                currentTab = item.title
                navController.navigate(item.destination.route) {
                    popUpTo(TabsRootScreenDestination.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            // The empty base entry shows on start, after logging in, or when back leaves a
            // tab's first screen. Back from Home leaves the app; from any other tab it goes
            // to Home with its history. Home is always restored, so no stale saved history
            // is left behind to block the next save.
            var hasOpenedTab by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(route) {
                if (route != TabsRootScreenDestination.route) {
                    // A login screen starts a new session, so the next base entry opens Home
                    if (route != null) hasOpenedTab = hideForRoutes.none { route.startsWith(it) }
                    return@LaunchedEffect
                }
                if (hasOpenedTab && currentTab == BottomNavItem.Home.title) {
                    finish()
                    return@LaunchedEffect
                }
                currentTab = BottomNavItem.Home.title
                navController.navigate(HomeScreenDestination.route) {
                    launchSingleTop = true
                    restoreState = true
                }
            }

            // Track sheet progress for nav bar animation
            var sheetProgress by remember { mutableFloatStateOf(0f) }

            // Track animation state for each nav item
            val animationStates = remember { mutableStateMapOf<BottomNavItem, Boolean>() }

            // Calculate nav bar animation values
            val navBarSlideProgress = (sheetProgress / 0.2f).coerceIn(0f, 1f)
            val navBarAlpha = 1f - navBarSlideProgress

            SwingMusicTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // Only show navigation bar when logged in and not on auth screens
                        if (showBottomNav) {
                            val density = LocalDensity.current
                            val navBarHeightPx = with(density) { 80.dp.toPx() }

                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset {
                                        IntOffset(
                                            0,
                                            (navBarHeightPx * navBarSlideProgress).roundToInt()
                                        )
                                    }
                                    .alpha(navBarAlpha),
                                containerColor = MaterialTheme.colorScheme.inverseOnSurface
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    bottomNavItems.forEach { item ->
                                        val isSelected = item.title == currentTab
                                        val atEnd = animationStates[item] ?: false

                                        NavigationBarItem(
                                            icon = {
                                                Icon(
                                                    painter = item.animatedIcon?.let { id ->
                                                        rememberAnimatedVectorPainter(
                                                            animatedImageVector = AnimatedImageVector.animatedVectorResource(id),
                                                            atEnd = atEnd
                                                        )
                                                    } ?: painterResource(
                                                        if (isSelected) item.selectedIcon else item.icon
                                                    ),
                                                    contentDescription = item.title
                                                )
                                            },
                                            selected = isSelected,
                                            alwaysShowLabel = false,
                                            label = { Text(text = item.title) },
                                            onClick = {
                                                animationStates[item] = !(animationStates[item] ?: false)
                                                selectTab(item)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { paddingValues ->
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
                            val navigator = CoreNavigator(navController)

                            // Always use AnimatedPlayerSheet - it handles "no track" case internally
                            AnimatedPlayerSheet(
                                paddingValues = paddingValues,
                                mediaControllerViewModel = mediaControllerViewModel,
                                navigator = navigator,
                                snackbarHostState = snackbarHostState,
                                onProgressChange = { progress ->
                                    sheetProgress = progress
                                }
                            ) {
                                SwingMusicAppNavigationWithAnimatedPlayer(
                                    isUserLoggedIn = value,
                                    navController = navController,
                                    authViewModel = authViewModel,
                                    mediaControllerViewModel = mediaControllerViewModel,
                                    searchViewModel = searchViewModel
                                )
                            }
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
internal fun SwingMusicAppNavigationWithAnimatedPlayer(
    isUserLoggedIn: Boolean,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mediaControllerViewModel: MediaControllerViewModel,
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
            dependency(mediaControllerViewModel)
            dependency(searchViewModel)
        }
    )
}
