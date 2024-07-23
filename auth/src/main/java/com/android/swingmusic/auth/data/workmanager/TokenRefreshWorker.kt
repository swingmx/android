package com.android.swingmusic.auth.data.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.swingmusic.auth.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

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
        return try {
            val freshTokens = authRepository.getFreshTokensFromServer()

            freshTokens?.let {
                authRepository.storeAuthTokens(
                    it.accessToken,
                    it.refreshToken,
                    it.maxAge
                )
            }

            if (freshTokens?.accessToken != null) Result.success() else Result.failure()

        } catch (e: HttpException) {
            Result.retry()

        } catch (e: Exception) {
            Result.failure()
        }
    }
}
