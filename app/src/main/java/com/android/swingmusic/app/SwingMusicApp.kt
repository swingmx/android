package com.android.swingmusic.app

import android.app.Application
import timber.log.Timber

class SwingMusicApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
