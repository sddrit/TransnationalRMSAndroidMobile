package com.tlrm.mobile.whapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tlrm.mobile.whapp.api.LocationApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.services.LocationService

class PallateSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = PickItemSyncWorker::class.java.simpleName

    private val database: AppDatabase = AppDatabase.getDatabase(appContext)

    private val locationService: LocationService = LocationService(
        ServiceGenerator.createService(LocationApiService::class.java),
        database.paletteDao()
    )

    override suspend fun doWork(): Result {
        val pallateId = inputData.getInt("pallate_id", 0)
        return try {
            val success = locationService.syncItem(pallateId)
            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to sync pallate details ${pallateId}", e)
            Result.failure()
        }
    }

}