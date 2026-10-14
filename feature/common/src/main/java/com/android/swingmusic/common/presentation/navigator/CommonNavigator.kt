package com.android.swingmusic.common.presentation.navigator

interface CommonNavigator {

    fun gotoLoginWithUsername()

    fun gotoLoginWithQrCode()

    fun gotoHome()

    fun gotoProfile()

    fun gotoStats()

    fun gotoLibraryManagement()

    fun gotoPairDevice()

    fun gotoAvatarCrop(imageUri: String)

    fun gotoFolders()

    fun gotoAllAlbums()

    fun gotoAllArtists()

    fun gotoPlaylists()

    fun gotoPlaylist(playlistId: String)

    fun gotoFavouriteTracks()

    fun gotoFavouriteAlbums()

    fun gotoFavouriteArtists()

    fun gotoSettings()

    fun gotoAccountSettings()

    fun gotoLyricsSettings()

    fun gotoStorageSettings()

    fun gotoAbout()

    fun gotoAlbumWithInfo(albumHash: String)

    fun navigateBack()

    fun gotoQueueScreen()

    fun gotoArtistInfo(artistHash: String)

    fun gotoViewAllOnArtistScreen(
        artistHash: String,
        viewAllType: String,
        artistName: String,
        baseUrl: String
    )

    fun gotoViewAllSearchResultsScreen(viewAllType: String, searchParams: String)

    fun gotoSourceFolder(name: String, path: String)

}
