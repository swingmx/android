package com.android.swingmusic.library.presentation.state

import androidx.compose.runtime.Immutable
import com.android.swingmusic.library.domain.FavouritesSummary

@Immutable
internal data class FavouritesUiState(
    val avatarPath: String? = null,
    val baseUrl: String = "",
    val isLoadingFavourites: Boolean = true,
    val errorLoadingFavourites: String? = null,
    val favourites: FavouritesSummary? = null,
)
