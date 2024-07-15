package com.android.swingmusic.auth.presentation.navigation

interface AuthNavigator {

    fun gotoLoginWithUsername()

    fun gotoLoginWithQrCode()

    fun gotoHomeNavGraph()

    // Todo: Remove this after adding home content
    fun gotoFolderNavGraph()
}
