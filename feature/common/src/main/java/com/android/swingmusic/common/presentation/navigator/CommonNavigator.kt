package com.android.swingmusic.common.presentation.navigator

interface CommonNavigator {

    fun gotoLoginWithUsername()

    fun gotoLoginWithQrCode()

    fun gotoHome()

    fun gotoProfile()

    fun gotoStats()

    fun gotoLibrary()

    fun gotoPairDevice()

    fun gotoFolders()

    fun gotoSettings()

    fun gotoAccountSettings()

    fun gotoLyricsSettings()

    fun gotoStorageSettings()

    fun gotoAbout()

    fun gotoAlbumWithInfo(albumHash: String)

    fun navigateBack()

    fun gotoQueueScreen()

    fun gotoArtistInfo(artistHash: String)

    fun gotoViewAllOnArtistScreen(viewAllType: String, artistName: String, baseUrl: String)

    fun gotoViewAllSearchResultsScreen(viewAllType: String, searchParams: String)

    fun gotoSourceFolder(name: String, path: String)

}
