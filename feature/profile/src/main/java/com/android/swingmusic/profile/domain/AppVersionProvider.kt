package com.android.swingmusic.profile.domain

/** The app's version name, so ViewModels don't need a Context for it. */
fun interface AppVersionProvider {
    fun get(): String
}
