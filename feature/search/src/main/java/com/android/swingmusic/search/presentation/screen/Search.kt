package com.android.swingmusic.search.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.android.swingmusic.common.presentation.navigator.CommonNavigator
import com.android.swingmusic.player.presentation.viewmodel.MediaControllerViewModel
import com.android.swingmusic.uicomponent.R
import com.android.swingmusic.uicomponent.presentation.theme.SwingMusicTheme
import com.ramcosta.composedestinations.annotation.Destination

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Search() {
    var searchParam by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchParam,
                    onQueryChange = { searchParam = it },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = {
                        Text(
                            text = "Search Swing Music",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5F)
                        )
                    },
                    leadingIcon = {
                        AnimatedVisibility(
                            visible = expanded,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            IconButton(onClick = {
                                expanded = false
                            }) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back Icon"
                                )
                            }
                        }

                        if (!expanded) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = R.drawable.swing_music_logo_outlined),
                                contentDescription = "App Icon"
                            )
                        }
                    },
                    trailingIcon = {
                        if (expanded) {
                            IconButton(onClick = {
                                expanded = false
                                searchParam = ""
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // Search results and Suggestions
            }
        }

        // Screen Content when SearchBar is collapsed
    }
}

@Destination
@Composable
fun SearchScreen(
    mediaControllerViewModel: MediaControllerViewModel, commonNavigator: CommonNavigator
) {
    SwingMusicTheme {
        Surface {
            Search()
        }
    }
}
