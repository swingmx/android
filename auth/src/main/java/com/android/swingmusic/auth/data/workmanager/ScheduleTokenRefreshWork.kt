package com.android.swingmusic.auth.data.workmanager

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleTokenRefreshWork(context: Context) {
    val workManager = WorkManager.getInstance(context)
    val workInfos = workManager.getWorkInfosForUniqueWork(TokenRefreshWorker.WORK_NAME).get()

    // .KEEP failed to work consistently, So, I added this check.
    if (workInfos.none { it.state in listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING)}) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val tokenRefreshWorkRequest = PeriodicWorkRequestBuilder<TokenRefreshWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15,
                TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            TokenRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            tokenRefreshWorkRequest
        )
    }
}
