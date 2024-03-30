package com.android.swingmusic.folder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.swingmusic.core.domain.model.FoldersAndTracks
import com.android.swingmusic.core.domain.model.FoldersAndTracksRequest
import com.android.swingmusic.core.util.Resource
import com.android.swingmusic.network.domain.repository.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
) : ViewModel() {

    // TODO: Make this a flow/state
    var _foldersAndTracks: List<FoldersAndTracks> = emptyList()

    fun getFoldersAndTracks() {
        viewModelScope.launch {
            // TODO: Replace this with the path provided by click events
            val path = "/home/eric/swing"
            val request = FoldersAndTracksRequest(path, false)

            when (networkRepository.getFoldersAndTracks(request)) {
                is Resource.Success -> {
                    Timber.e("Getting folders...")
                }

                else -> {
                    Timber.e("Error Getting folders...")
                }
            }
        }
    }

    init {
        Timber.e("View model init...")
    }
}
