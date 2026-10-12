package com.android.swingmusic.library.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.library.presentation.event.FavouritesUiEffect
import com.android.swingmusic.library.presentation.event.FavouritesUiEvent
import com.android.swingmusic.library.presentation.state.FavouritesUiState
import com.android.swingmusic.library.presentation.viewmodel.FavouritesViewModel
import com.android.swingmusic.uicomponent.presentation.component.LargeTitle
import com.android.swingmusic.uicomponent.presentation.component.LargeTitleTopBar
import com.android.swingmusic.uicomponent.presentation.component.largeTitleTopBarHeight
import com.android.swingmusic.uicomponent.presentation.component.rememberLargeTitleState
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
import com.ramcosta.composedestinations.annotation.Destination

private const val FAVOURITES_TITLE = "Favourites"

@Destination
@Composable
fun FavouritesScreen(
    navigator: CommonNavigator,
) {
    Favourites(navigator = navigator)
}

@Composable
private fun Favourites(
    navigator: CommonNavigator,
    viewModel: FavouritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            FavouritesUiEffect.NavigateToProfile -> navigator.gotoProfile()
        }
    }

    FavouritesScreenContent(uiState = uiState, onEvent = viewModel::onEvent)
}

@Composable
private fun FavouritesScreenContent(
    uiState: FavouritesUiState,
    onEvent: (FavouritesUiEvent) -> Unit,
) {
    val listState = rememberLazyListState()
    val titleState = rememberLargeTitleState(listState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = largeTitleTopBarHeight(), bottom = 200.dp)
        ) {
            item(key = "title") {
                LargeTitle(text = FAVOURITES_TITLE, state = titleState)
            }
            item(key = "placeholder") {
                Text(
                    text = "Your favourite tracks, albums and artists will show here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        LargeTitleTopBar(
            title = FAVOURITES_TITLE,
            state = titleState,
            avatarPath = uiState.avatarPath,
            onProfileClick = { onEvent(FavouritesUiEvent.OnProfileClicked) }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun FavouritesScreenContentPreview() {
    SwingMusicTheme {
        FavouritesScreenContent(uiState = FavouritesUiState(), onEvent = {})
    }
}
