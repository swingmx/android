package com.android.swingmusic.album.presentation.navigator

interface AlbumNavigator {

    fun gotoAlbumWithInfo(albumHash: String)

    fun navigateBack()
}
