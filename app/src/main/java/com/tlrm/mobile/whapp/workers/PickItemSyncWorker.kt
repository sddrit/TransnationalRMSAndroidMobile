package com.tlrm.mobile.whapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.services.PickListService

class PickItemSyncWorker (appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    private val TAG = PickItemSyncWorker::class.java.simpleName

    private val pickListService: PickListService =
        PickListService(ServiceGenerator.createService(PickListApiService::class.java),
            AppDatabase.getDatabase(appContext).pickListDao())

    override suspend fun doWork(): Result {
        val cartonNo = inputData.getString("carton_no")
        return try {
            val success = pickListService.syncedItem(cartonNo!!)
            if (success) {
                Result.success()
            }else {
                Result.retry()
            }
        }catch (e: Exception) {
            Log.e(TAG, "Unable to mark carton number ${cartonNo} as picked worker failed", e)
            Result.failure()
        }
    }

}