package com.android.swingmusic.common.presentation.navigator

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Tells a tab's root screen that its bottom-bar item was tapped while the screen was
 * already showing, so it can scroll to the top (or focus the search field).
 */
class TabReselect {
    enum class Tab { HOME, LIBRARY, FAVOURITES, SEARCH }

    private val _events = MutableSharedFlow<Tab>(extraBufferCapacity = 1)
    val events: SharedFlow<Tab> = _events.asSharedFlow()

    fun reselect(tab: Tab) {
        _events.tryEmit(tab)
    }
}
