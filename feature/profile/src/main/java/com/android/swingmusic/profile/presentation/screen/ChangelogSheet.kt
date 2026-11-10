package com.android.swingmusic.profile.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.core.domain.model.Change
import com.android.swingmusic.core.domain.model.ChangeLink
import com.android.swingmusic.core.domain.model.ChangeType
import com.android.swingmusic.core.domain.model.Changelog
import com.android.swingmusic.core.domain.model.Release
import com.android.swingmusic.core.domain.model.ReleaseChannel
import com.android.swingmusic.profile.presentation.event.ChangelogUiEffect
import com.android.swingmusic.profile.presentation.event.ChangelogUiEvent
import com.android.swingmusic.profile.presentation.state.ChangelogUiState
import com.android.swingmusic.profile.presentation.viewmodel.ChangelogViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * The What's New sheet. With [autoShowUnseen] it opens by itself the first time a build runs
 * (the app shell on Home); [openRequested] opens it on demand (Settings › About). Closing
 * marks the running build's notes as seen either way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogHost(
    openRequested: Boolean = false,
    autoShowUnseen: Boolean = !openRequested,
    onClosed: () -> Unit = {},
) {
    val viewModel: ChangelogViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            is ChangelogUiEffect.OpenUrl -> runCatching { uriHandler.openUri(effect.url) }
        }
    }

    val visible = uiState.changelog != null && (openRequested || (autoShowUnseen && uiState.isUnseen))
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val scope = rememberCoroutineScope()
    val close = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            viewModel.onEvent(ChangelogUiEvent.Dismissed)
            onClosed()
        }
        Unit
    }

    ModalBottomSheet(
        onDismissRequest = close,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Black.copy(alpha = .75F),
        properties = ModalBottomSheetProperties(shouldDismissOnClickOutside = false),
    ) {
        ChangelogSheetContent(
            uiState = uiState,
            onEvent = { event ->
                if (event == ChangelogUiEvent.Dismissed) close() else viewModel.onEvent(event)
            }
        )
    }
}

@Composable
internal fun ChangelogSheetContent(
    uiState: ChangelogUiState,
    onEvent: (ChangelogUiEvent) -> Unit,
) {
    val changelog = uiState.changelog ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1F)) {
                    Text(
                        text = if (uiState.isFirstLaunch) "Welcome to Swing Music" else "What's New",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.isFirstLaunch) {
                            "Here's what version ${changelog.current.versionName} brings"
                        } else {
                            "Version ${changelog.current.versionName} · ${changelog.current.date.toDisplayDate()}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onEvent(ChangelogUiEvent.Dismissed) }) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                }
            }
        }

        item {
            CurrentReleaseCard(
                release = changelog.current,
                showAllChanges = uiState.showAllChanges,
                onEvent = onEvent
            )
        }

        if (changelog.earlier.isNotEmpty()) {
            item {
                Text(
                    text = "Earlier",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 28.dp, bottom = 4.dp)
                )
            }
            items(changelog.earlier, key = { it.versionCode }) { release ->
                EarlierReleaseRow(
                    release = release,
                    expanded = release.versionCode in uiState.expandedVersionCodes,
                    isLast = release == changelog.earlier.last(),
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun CurrentReleaseCard(
    release: Release,
    showAllChanges: Boolean,
    onEvent: (ChangelogUiEvent) -> Unit,
) {
    val highlights = release.highlights
    val collapsed = highlights.ifEmpty { release.changes.take(3) }
    val shown = if (showAllChanges || collapsed.size == release.changes.size) release.changes else collapsed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .08F), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VersionChip(release)
            if (release.channel == ReleaseChannel.BETA) {
                Pill(text = "Beta", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        CountPills(release)
        Spacer(modifier = Modifier.height(16.dp))

        shown.forEachIndexed { index, change ->
            ChangeRow(change = change, onLinkClick = { onEvent(ChangelogUiEvent.LinkClicked(it)) })
            if (index != shown.lastIndex) Spacer(modifier = Modifier.height(12.dp))
        }

        if (collapsed.size < release.changes.size) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (showAllChanges) "Show less" else "Show all ${release.changes.size} changes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEvent(ChangelogUiEvent.ShowAllChangesToggled) }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun EarlierReleaseRow(
    release: Release,
    expanded: Boolean,
    isLast: Boolean,
    onEvent: (ChangelogUiEvent) -> Unit,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1F)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = .12F))
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1F)
                .padding(bottom = if (isLast) 0.dp else 8.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onEvent(ChangelogUiEvent.ReleaseToggled(release.versionCode)) }
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1F)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Version ${release.versionName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (release.channel == ReleaseChannel.BETA) {
                            Pill(text = "Beta", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = "${release.date.toDisplayDate()} · ${release.countSummary()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(if (expanded) 180F else 0F)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    release.changes.forEach { change ->
                        ChangeRow(change = change, onLinkClick = { onEvent(ChangelogUiEvent.LinkClicked(it)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeRow(change: Change, onLinkClick: (String) -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        TypePill(type = change.type, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = change.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (change.highlight) FontWeight.Medium else FontWeight.Normal
            )
            change.link?.let { link -> LinkChip(link = link, onClick = { onLinkClick(link.url) }) }
        }
    }
}

@Composable
private fun LinkChip(link: ChangeLink, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = link.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun VersionChip(release: Release) {
    Text(
        text = "v${release.versionName}",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun CountPills(release: Release) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChangeType.entries.forEach { type ->
            val count = release.count(type)
            if (count > 0) Pill(text = "$count ${type.label(count)}", color = type.colour())
        }
    }
}

@Composable
private fun TypePill(type: ChangeType, modifier: Modifier = Modifier) {
    Pill(text = type.label(1), color = type.colour(), modifier = modifier.width(52.dp))
}

@Composable
private fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = .14F))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

@Composable
private fun ChangeType.colour(): Color = when (this) {
    ChangeType.NEW -> Color(0xFF4ADE80)
    ChangeType.TWEAK -> Color(0xFF93C5FD)
    ChangeType.FIXED -> Color(0xFFFCD34D)
}

private fun ChangeType.label(count: Int): String = when (this) {
    ChangeType.NEW -> "New"
    ChangeType.TWEAK -> if (count == 1) "Tweak" else "Tweaks"
    ChangeType.FIXED -> "Fixed"
}

private fun Release.countSummary(): String =
    ChangeType.entries
        .map { it to count(it) }
        .filter { it.second > 0 }
        .joinToString(" · ") { (type, count) -> "$count ${type.label(count).lowercase()}" }

private fun String.toDisplayDate(): String = runCatching {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(this) ?: return this
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(parsed)
}.getOrDefault(this)

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun ChangelogSheetContentPreview() {
    val current = Release(
        versionName = "2.1.0", versionCode = 20100, date = "2026-11-15", channel = ReleaseChannel.STABLE,
        changes = listOf(
            Change(ChangeType.NEW, "Your listening stats on every artist page", highlight = true,
                link = ChangeLink("Details", "https://github.com/swingmx/android/issues/159")),
            Change(ChangeType.NEW, "Drag to reorder the queue", highlight = true),
            Change(ChangeType.TWEAK, "Recently played cards open the item's page"),
            Change(ChangeType.FIXED, "Shuffle no longer restarts the current track"),
        )
    )
    val earlier = Release(
        versionName = "2.0.0", versionCode = 20000, date = "2026-11-06", channel = ReleaseChannel.STABLE,
        changes = listOf(Change(ChangeType.NEW, "Lyrics overlay with word-by-word sync"))
    )
    SwingMusicTheme {
        ChangelogSheetContent(
            uiState = ChangelogUiState(changelog = Changelog(current, listOf(earlier)), expandedVersionCodes = setOf(20000)),
            onEvent = {}
        )
    }
}
