package com.android.swingmusic.auth.data.workmanager

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleTokenRefreshWork(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val tokenRefreshWorkRequest = PeriodicWorkRequestBuilder<TokenRefreshWorker>(21, TimeUnit.DAYS)
        .setConstraints(constraints)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            1,
            TimeUnit.HOURS
        )
        .build()

    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            TokenRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            tokenRefreshWorkRequest
        )
}
