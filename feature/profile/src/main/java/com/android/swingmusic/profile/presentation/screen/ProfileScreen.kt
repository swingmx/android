package com.android.swingmusic.profile.presentation.screen

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.core.domain.model.StatItem
import com.android.swingmusic.database.domain.model.User
import com.android.swingmusic.profile.presentation.component.ProfileSnackbarHost
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.component.SettingsGroup
import com.android.swingmusic.profile.presentation.component.SettingsGroupDivider
import com.android.swingmusic.profile.presentation.component.SeeAllBadge
import com.android.swingmusic.profile.presentation.component.SettingsRow
import com.android.swingmusic.profile.presentation.component.SkeletonBlock
import com.android.swingmusic.profile.presentation.component.rememberSkeletonAlpha
import com.android.swingmusic.profile.presentation.event.ProfileUiEffect
import com.android.swingmusic.profile.presentation.event.ProfileUiEvent
import com.android.swingmusic.profile.presentation.state.ProfileUiState
import com.android.swingmusic.profile.presentation.state.displayName
import com.android.swingmusic.profile.presentation.state.initials
import com.android.swingmusic.profile.presentation.state.isAdmin
import com.android.swingmusic.profile.presentation.state.serverHost
import com.android.swingmusic.profile.presentation.viewmodel.ProfileViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import java.io.File

@Destination
@Composable
internal fun ProfileScreen(
    navigator: CommonNavigator,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    // ACTION_GET_CONTENT: Android picks the UI and the sources (photo picker, Google Photos, Files…).
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onEvent(ProfileUiEvent.OnPhotoPicked(it.toString())) }
    }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            ProfileUiEffect.OpenPhotoPicker -> photoPicker.launch("image/*")
            is ProfileUiEffect.NavigateToAvatarCrop -> navigator.gotoAvatarCrop(effect.imageUri)
            ProfileUiEffect.ShowPhotoRemoved -> scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Profile photo removed",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short
                )
                viewModel.onEvent(
                    if (result == SnackbarResult.ActionPerformed) ProfileUiEvent.OnUndoRemovePhoto
                    else ProfileUiEvent.OnPhotoRemovalSettled
                )
            }

            ProfileUiEffect.NavigateBack -> navigator.navigateBack()
            ProfileUiEffect.NavigateToStats -> navigator.gotoStats()
            ProfileUiEffect.NavigateToLibrary -> navigator.gotoLibrary()
            ProfileUiEffect.NavigateToPairDevice -> navigator.gotoPairDevice()
            ProfileUiEffect.NavigateToSettings -> navigator.gotoSettings()
            is ProfileUiEffect.CopyToClipboard -> {
                clipboard.setText(AnnotatedString(effect.text))
                // Android 13+ shows its own clipboard confirmation.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    scope.launch { snackbarHostState.showSnackbar("Server address copied") }
                }
            }
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { ProfileSnackbarHost(snackbarHostState) },
        topBar = { ProfileTopBar(title = "Profile", onBack = { onEvent(ProfileUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(0.dp))

            ProfileHeader(
                user = uiState.user,
                avatarPath = uiState.avatarPath,
                serverHost = serverHost(uiState.baseUrl),
                onAvatarClick = { onEvent(ProfileUiEvent.OnAvatarClicked) },
                onCopyServer = { onEvent(ProfileUiEvent.OnCopyServerClicked) }
            )

            WeeklyStats(
                uiState = uiState,
                onRetry = { onEvent(ProfileUiEvent.OnRetryStats) },
                onSeeAll = { onEvent(ProfileUiEvent.OnStatsClicked) }
            )

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Rounded.BarChart,
                    title = "Listening stats",
                    subtitle = "Top tracks, artists and albums",
                    onClick = { onEvent(ProfileUiEvent.OnStatsClicked) }
                )
                SettingsGroupDivider()
                SettingsRow(
                    icon = Icons.Rounded.Folder,
                    title = "Library",
                    subtitle = if (uiState.user?.isAdmin == true) "Folders, scan, root folders" else "Browse folders",
                    onClick = { onEvent(ProfileUiEvent.OnLibraryClicked) }
                )
                SettingsGroupDivider()
                SettingsRow(
                    icon = Icons.Rounded.QrCode2,
                    title = "Pair another device",
                    subtitle = "Show a QR code to sign in elsewhere",
                    onClick = { onEvent(ProfileUiEvent.OnPairDeviceClicked) }
                )
                SettingsGroupDivider()
                SettingsRow(
                    icon = Icons.Rounded.Settings,
                    title = "Settings",
                    subtitle = "Account, lyrics, storage, about",
                    onClick = { onEvent(ProfileUiEvent.OnSettingsClicked) }
                )
            }

            SettingsGroup(containerColor = MaterialTheme.colorScheme.error.copy(alpha = .1F)) {
                SettingsRow(
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    title = "Log out",
                    contentColor = MaterialTheme.colorScheme.error,
                    showChevron = false,
                    onClick = { onEvent(ProfileUiEvent.OnLogOutClicked) }
                )
            }

            Text(
                text = versionsLine(uiState.appVersion, uiState.serverVersion),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Clears the mini player and navigation bar.
            Spacer(modifier = Modifier.height(160.dp))
        }
    }

    if (uiState.showPhotoViewer) {
        PhotoViewer(
            username = uiState.user?.displayName.orEmpty(),
            initials = uiState.user?.initials.orEmpty(),
            avatarPath = uiState.avatarPath,
            onChange = { onEvent(ProfileUiEvent.OnChangePhoto) },
            onRemove = { onEvent(ProfileUiEvent.OnRemovePhoto) },
            onDismiss = { onEvent(ProfileUiEvent.OnPhotoViewerDismissed) }
        )
    }

    if (uiState.showLogOutDialog) {
        LogOutDialog(
            isLoggingOut = uiState.isLoggingOut,
            onConfirm = { onEvent(ProfileUiEvent.OnLogOutConfirmed) },
            onDismiss = { onEvent(ProfileUiEvent.OnLogOutDismissed) }
        )
    }
}

@Composable
private fun ProfileHeader(
    user: User?,
    avatarPath: String?,
    serverHost: String,
    onAvatarClick: () -> Unit,
    onCopyServer: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ProfileAvatar(
            initials = user?.initials.orEmpty(),
            avatarPath = avatarPath,
            size = 72.dp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClickLabel = "Change profile photo", onClick = onAvatarClick)
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = user?.displayName.orEmpty(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F, fill = false)
                )
                if (user?.isAdmin == true) AdminChip()
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClickLabel = "Copy server address", onClick = onCopyServer)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val tint = MaterialTheme.colorScheme.onSurfaceVariant
                Icon(Icons.Rounded.Language, null, tint = tint, modifier = Modifier.size(16.dp))
                Text(
                    text = serverHost,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1F, fill = false)
                )
                Icon(Icons.Rounded.ContentCopy, null, tint = tint, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    initials: String,
    avatarPath: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        if (avatarPath != null) {
            AsyncImage(
                model = File(avatarPath),
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(text = initials, fontSize = (size.value * .36F).sp, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * The photo enlarged in the centre over a dark scrim, with Change and Remove.
 * A full-screen dialog, so the scrim also covers the nav bar and mini player.
 */
@Composable
private fun PhotoViewer(
    username: String,
    initials: String,
    avatarPath: String?,
    onChange: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    // What to run once the exit animation finishes: a dismiss, a change or a remove.
    var afterExit by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun close(then: () -> Unit) {
        if (afterExit != null) return
        afterExit = then
        visible.targetState = false
    }

    LaunchedEffect(visible.isIdle, visible.currentState) {
        if (visible.isIdle && !visible.currentState) afterExit?.invoke()
    }

    Dialog(
        onDismissRequest = { close(onDismiss) },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(180))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = .86F))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { close(onDismiss) }
                        )
                )
            }

            AnimatedVisibility(
                visibleState = visible,
                modifier = Modifier.align(Alignment.Center),
                enter = fadeIn(tween(220)) + scaleIn(tween(260, easing = FastOutSlowInEasing), initialScale = .6F),
                exit = fadeOut(tween(160)) + scaleOut(tween(180), targetScale = .8F)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileAvatar(
                        initials = initials,
                        avatarPath = avatarPath,
                        size = 264.dp,
                        modifier = Modifier.shadow(24.dp, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = username, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Only on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { close(onChange) },
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = .12F),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change")
                        }
                        Button(
                            onClick = { close(onRemove) },
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = .14F),
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdminChip() {
    Text(
        text = "ADMIN",
        modifier = Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = .18F),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = .4.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .84F)
    )
}

private data class StatCard(
    val label: String,
    val value: String,
    val subtitle: String? = null,
    val image: String? = null
)

private val leadingNumber = Regex("^[\\d.,]+")

private fun StatItem.toStatCard(): StatCard? = when (type) {
    "playtime" -> StatCard("Listening time", value.removeSuffix(" listened"))
    "streams" -> StatCard("Plays", leadingNumber.find(value)?.value ?: value)
    "favorites" -> StatCard("New favourites", leadingNumber.find(value)?.value ?: value)
    "toptrack" -> {
        val title = value.substringBeforeLast(" - ")
        val artist = value.substringAfterLast(" - ", missingDelimiterValue = "")
        StatCard("Top track", title, artist.ifBlank { null }, image)
    }

    else -> null
}

private val statOrder = listOf("playtime", "streams", "favorites", "toptrack")

@Composable
private fun WeeklyStats(uiState: ProfileUiState, onRetry: () -> Unit, onSeeAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "This week",
                modifier = Modifier.weight(1F),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            SeeAllBadge(text = "See All", onClick = onSeeAll)
        }

        when {
            uiState.isLoadingStats -> StatGrid(List(4) { null }, uiState.baseUrl)

            uiState.errorLoadingStats != null -> StatsError(uiState.errorLoadingStats, onRetry)

            else -> {
                val cards = uiState.stats
                    .sortedBy { statOrder.indexOf(it.type) }
                    .mapNotNull { it.toStatCard() }
                StatGrid(cards, uiState.baseUrl)
            }
        }
    }
}

@Composable
private fun StatGrid(cards: List<StatCard?>, baseUrl: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { card ->
                    StatCardView(card, baseUrl, Modifier.weight(1F))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1F))
            }
        }
    }
}

@Composable
private fun StatCardView(card: StatCard?, baseUrl: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(92.dp)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .27F), shape)
    ) {
        if (card == null) {
            StatCardSkeleton()
            return@Box
        }

        val hasImage = card.image != null
        if (hasImage) {
            AsyncImage(
                model = "${baseUrl}img/thumbnail/small/${card.image}",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = .62F))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = card.label,
                style = MaterialTheme.typography.bodySmall,
                color = if (hasImage) Color.White.copy(alpha = .84F)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = card.value,
                    fontSize = if (card.subtitle != null) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasImage) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                card.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = .84F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCardSkeleton() {
    val alpha = rememberSkeletonAlpha()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SkeletonBlock(modifier = Modifier.size(width = 72.dp, height = 10.dp))
        SkeletonBlock(modifier = Modifier.size(width = 104.dp, height = 18.dp))
    }
}

@Composable
private fun StatsError(message: String, onRetry: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .1F), shape)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.WifiOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = "Couldn't load your stats", fontWeight = FontWeight.Medium)
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            onClick = onRetry,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .3F))
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun LogOutDialog(isLoggingOut: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null) },
        title = { Text("Log out?") },
        text = {
            Text(
                "You'll need to scan a pairing code or log in again. Playback stops and your " +
                    "queue is cleared.\n\nThe server address stays filled in on the login screen."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoggingOut) {
                if (isLoggingOut) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Log out", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoggingOut) { Text("Cancel") }
        }
    )
}

private fun versionsLine(appVersion: String, serverVersion: String?): String {
    val app = "Swing Music $appVersion".trim()
    val server = serverVersion?.let { "Server ${if (it.startsWith("v")) it else "v$it"}" }
    return listOfNotNull(app, server).joinToString(" · ")
}

private val previewUser = User(
    email = "",
    firstname = "",
    id = 1,
    image = "",
    lastname = "",
    roles = listOf("admin"),
    username = "eric"
)

private val previewStats = listOf(
    StatItem("toptrack", "Alive - Sia", "Top track this week", null),
    StatItem("streams", "340 track plays", "this week", null),
    StatItem("playtime", "12 hr 40 min listened", "this week", null),
    StatItem("favorites", "18 new favorites", "this week", null),
)

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, heightDp = 900)
@Composable
private fun ProfileScreenContentPreview() {
    SwingMusicTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                baseUrl = "https://neon.example.com/",
                user = previewUser,
                isLoadingStats = false,
                stats = previewStats,
                appVersion = "1.0.0",
                serverVersion = "2.1.14"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A, heightDp = 900)
@Composable
private fun ProfileScreenErrorPreview() {
    SwingMusicTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                baseUrl = "http://192.168.1.20:1970/",
                user = previewUser.copy(roles = emptyList(), username = "kim"),
                isLoadingStats = false,
                errorLoadingStats = "Check your connection to the server.",
                appVersion = "1.0.0"
            ),
            onEvent = {}
        )
    }
}
