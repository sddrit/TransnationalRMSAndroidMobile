package com.tlrm.mobile.whapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.services.SessionService

class RemovePickListSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = RemovePickListSyncWorker::class.java.simpleName

    private val pickListService: PickListService =
        PickListService(
            ServiceGenerator.createService(PickListApiService::class.java),
            DeviceService(
                ServiceGenerator.createService(DeviceApiService::class.java),
                SessionService(this.applicationContext)
            ),
            AppDatabase.getDatabase(appContext).pickListDao()
        )

    override suspend fun doWork(): Result {
        return try {
            pickListService.deleteCompletedPickList()
            Result.success();
        } catch (e: Exception) {
            Log.e(TAG, "Unable to delete completed picklist", e)
            Result.retry()
        }
    }

}