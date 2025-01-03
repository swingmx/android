package com.android.swingmusic.common.presentation.navigator

interface CommonNavigator {

    fun gotoLoginWithUsername()

    fun gotoLoginWithQrCode()

    fun gotoHomeNavGraph()

    // Todo: Remove this after adding home content
    fun gotoFolderNavGraph()

    fun gotoAlbumWithInfo(albumHash: String)

    fun navigateBack()

    fun gotoQueueScreen()

    fun gotoArtistInfo(artistHash: String)

    fun gotoViewAllScreen(viewAllType: String, artistName: String, baseUrl: String)

    fun gotoSourceFolder(name: String, path: String)
}
