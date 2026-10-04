package com.android.swingmusic.profile.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.profile.presentation.component.ProfileSnackbarHost
import com.android.swingmusic.profile.presentation.component.ProfileTopBar
import com.android.swingmusic.profile.presentation.component.SettingsGroup
import com.android.swingmusic.profile.presentation.event.StorageUiEffect
import com.android.swingmusic.profile.presentation.event.StorageUiEvent
import com.android.swingmusic.profile.presentation.state.StorageUiState
import com.android.swingmusic.profile.presentation.util.formatBytes
import com.android.swingmusic.profile.presentation.viewmodel.StorageViewModel
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@Destination
@Composable
internal fun StorageScreen(
    navigator: CommonNavigator,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            StorageUiEffect.NavigateBack -> navigator.navigateBack()
            is StorageUiEffect.ShowSnackBar -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    StorageScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun StorageScreenContent(
    uiState: StorageUiState,
    onEvent: (StorageUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { ProfileSnackbarHost(snackbarHostState) },
        topBar = { ProfileTopBar(title = "Storage", onBack = { onEvent(StorageUiEvent.OnBackClicked) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SettingsGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1F)) {
                        Text(text = "Image cache", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = (uiState.imageCacheBytes?.let { "${formatBytes(it)} · " } ?: "") +
                                "artwork reloads from the server when needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedButton(
                        onClick = { onEvent(StorageUiEvent.OnClearImageCache) },
                        enabled = !uiState.isClearing && (uiState.imageCacheBytes ?: 0L) > 0L,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .3F))
                    ) {
                        if (uiState.isClearing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun StorageScreenContentPreview() {
    SwingMusicTheme {
        StorageScreenContent(uiState = StorageUiState(imageCacheBytes = 48L * 1024 * 1024), onEvent = {})
    }
}
