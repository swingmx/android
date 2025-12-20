package com.android.swingmusic.app

import android.app.Application
import androidx.work.Configuration
import com.android.swingmusic.auth.data.workmanager.TokenRefreshWorkerFactory
import com.android.swingmusic.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SwingMusicApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: TokenRefreshWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override val workManagerConfiguration: Configuration by lazy {
        Configuration.Builder()
            .setExecutor(Dispatchers.Default.asExecutor())
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }
}
