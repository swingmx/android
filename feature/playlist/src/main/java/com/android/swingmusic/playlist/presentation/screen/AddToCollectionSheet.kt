package com.android.swingmusic.playlist.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.playlist.domain.Collection
import com.android.swingmusic.playlist.domain.CollectionTarget
import com.android.swingmusic.playlist.presentation.event.AddToCollectionUiEffect
import com.android.swingmusic.playlist.presentation.event.AddToCollectionUiEvent
import com.android.swingmusic.playlist.presentation.state.AddToCollectionUiState
import com.android.swingmusic.playlist.presentation.viewmodel.AddToCollectionViewModel
import com.android.swingmusic.uicomponent.presentation.component.FilledTextField
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import kotlinx.coroutines.launch

/**
 * Picks a collection to add [target] to, or creates a new one from it.
 * [onDone] receives a confirmation message once the sheet has closed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionSheet(
    target: CollectionTarget,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    onDone: (message: String) -> Unit,
) {
    // A fresh key per opening, so the list is reloaded every time the sheet is shown.
    val key = remember { "add-to-collection:${System.nanoTime()}" }
    val viewModel = hiltViewModel<AddToCollectionViewModel, AddToCollectionViewModel.Factory>(
        key = key,
        creationCallback = { factory -> factory.create(target) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            is AddToCollectionUiEffect.Done -> {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    onDismiss()
                    onDone(effect.message)
                }
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        shape = RoundedCornerShape(16.dp),
        onDismissRequest = onDismiss,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = .75F),
        tonalElevation = 16.dp,
    ) {
        AddToCollectionContent(
            uiState = uiState,
            onBack = {
                if (uiState.showNewCollectionField) {
                    viewModel.onEvent(AddToCollectionUiEvent.OnCloseNewCollection)
                } else {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                        onBack()
                    }
                }
            },
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
private fun AddToCollectionContent(
    uiState: AddToCollectionUiState,
    onBack: () -> Unit,
    onEvent: (AddToCollectionUiEvent) -> Unit,
) {
    BackHandler(enabled = uiState.showNewCollectionField) {
        onEvent(AddToCollectionUiEvent.OnCloseNewCollection)
    }
    Column(modifier = Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Add to Collection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(modifier = Modifier.height(2.dp)) {
            if (uiState.isSaving) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        uiState.errorAddingToCollection?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (uiState.showNewCollectionField) {
            NewCollectionField(uiState = uiState, onEvent = onEvent)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(AddToCollectionUiEvent.OnNewCollectionClicked) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconTile { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "New Collection", style = MaterialTheme.typography.bodyLarge)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06F))

        when {
            uiState.isLoadingCollections -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }

            uiState.errorLoadingCollections != null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.errorLoadingCollections,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onEvent(AddToCollectionUiEvent.OnRetry) }) {
                    Text(text = "Retry")
                }
            }

            uiState.collections.isEmpty() -> Text(
                text = "No collections yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )

            else -> LazyColumn(modifier = Modifier.weight(1F)) {
                itemsIndexed(uiState.collections, key = { _, collection -> collection.id }) { index, collection ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .06F)
                        )
                    }
                    CollectionRow(
                        collection = collection,
                        onClick = { onEvent(AddToCollectionUiEvent.OnCollectionClicked(collection)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NewCollectionField(
    uiState: AddToCollectionUiState,
    onEvent: (AddToCollectionUiEvent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        FilledTextField(
            value = uiState.newCollectionName,
            onValueChange = { onEvent(AddToCollectionUiEvent.OnNewCollectionNameChanged(it)) },
            placeholder = "Collection name",
            modifier = Modifier
                .weight(1F)
                .focusRequester(focusRequester),
            errorText = uiState.newCollectionError,
            onDone = { onEvent(AddToCollectionUiEvent.OnCreateCollection) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledIconButton(
            onClick = { onEvent(AddToCollectionUiEvent.OnCreateCollection) },
            enabled = !uiState.isSaving && uiState.newCollectionName.isNotBlank(),
            modifier = Modifier
                .padding(top = 4.dp)
                .size(48.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Check, contentDescription = "Create collection")
        }
    }
}

@Composable
private fun CollectionRow(collection: Collection, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile {
            Icon(
                imageVector = Icons.Rounded.LibraryMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (collection.itemCount == 1) "1 item" else "${collection.itemCount} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IconTile(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun AddToCollectionContentPreview() {
    SwingMusicTheme {
        AddToCollectionContent(
            uiState = AddToCollectionUiState(
                isLoadingCollections = false,
                collections = listOf(
                    Collection(id = 1, name = "Sunday mornings", description = "", itemCount = 12),
                    Collection(id = 2, name = "Jazz", description = "", itemCount = 1),
                )
            ),
            onBack = {},
            onEvent = {}
        )
    }
}
