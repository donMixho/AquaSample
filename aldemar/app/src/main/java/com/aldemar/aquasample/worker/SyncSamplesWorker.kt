package com.aldemar.aquasample.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aldemar.aquasample.AquaSampleApp
import com.aldemar.aquasample.data.remote.backend.BackendClient
import com.aldemar.aquasample.data.remote.backend.SampleSyncer
import java.util.concurrent.TimeUnit

class SyncSamplesWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as AquaSampleApp
        val api = BackendClient.create(app.settings.backendUrl())
        val r = SampleSyncer(app.repository, api).sync()
        return if (r.failed > 0) Result.retry() else Result.success()
    }
}

object SyncScheduler {
    private const val PERIODIC = "sync_samples_periodic"
    private const val NOW = "sync_samples_now"
    private val connected = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    /** Sincronización periódica (cada 15 min, mínimo de WorkManager) cuando hay red. */
    fun schedulePeriodic(ctx: Context) {
        val req = PeriodicWorkRequestBuilder<SyncSamplesWorker>(15, TimeUnit.MINUTES)
            .setConstraints(connected).build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    /** Disparo inmediato (al crear/editar/revisar); se ejecuta en cuanto haya red. */
    fun syncNow(ctx: Context) {
        val req = OneTimeWorkRequestBuilder<SyncSamplesWorker>().setConstraints(connected).build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(NOW, ExistingWorkPolicy.APPEND_OR_REPLACE, req)
    }
}
