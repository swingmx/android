package com.android.swingmusic.auth.data.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.swingmusic.auth.domain.model.SessionEndReason
import com.android.swingmusic.auth.domain.model.TokenRefreshResult
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TokenRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val authRepository: AuthRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "TokenRefreshWorker"
    }

    override suspend fun doWork(): Result {
        return when (authRepository.refreshTokens()) {
            is TokenRefreshResult.Refreshed -> Result.success()
            is TokenRefreshResult.Failed -> Result.retry()
            is TokenRefreshResult.Rejected -> {
                authRepository.endSession(SessionEndReason.EXPIRED)
                Result.failure()
            }
        }
    }
}
