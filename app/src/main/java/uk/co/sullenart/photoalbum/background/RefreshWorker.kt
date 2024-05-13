package uk.co.sullenart.photoalbum.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import timber.log.Timber

class RefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {

        val fetcher: BackgroundFetcher = get()
        fetcher.refresh()

        return Result.success()
    }
}