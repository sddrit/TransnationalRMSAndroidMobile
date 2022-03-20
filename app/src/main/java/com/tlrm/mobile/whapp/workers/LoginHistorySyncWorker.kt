package com.tlrm.mobile.whapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.api.UserApiService
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.services.UserService

class LoginHistorySyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = PickItemSyncWorker::class.java.simpleName

    private val database: AppDatabase = AppDatabase.getDatabase(appContext)

    private val userService: UserService = UserService(
        ServiceGenerator.createService(UserApiService::class.java),
        database.userDao()
    )

    override suspend fun doWork(): Result {
        val userId = inputData.getInt("userId", 0)
        val loginDate = inputData.getString("loginDate")
        val hostName = inputData.getString("hostName");
        return try {
            val success = userService.addLoginHistory(userId, loginDate!!, hostName!!)
            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to sync login history", e)
            Result.failure()
        }
    }

}