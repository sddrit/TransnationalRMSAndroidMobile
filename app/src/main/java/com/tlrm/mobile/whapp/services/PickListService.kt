package com.tlrm.mobile.whapp.services

import android.util.Log
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.database.dao.PickListDao
import com.tlrm.mobile.whapp.database.dao.PickListDetailsItem
import com.tlrm.mobile.whapp.database.entities.PickListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PickListService(private val pickListApiService: PickListApiService,
    private val pickListDao: PickListDao) {

    suspend fun getPickListDetailItems(): List<PickListDetailsItem> {
        return withContext(Dispatchers.IO) {
            return@withContext pickListDao.getPickListDetailItems()
        }
    }

    suspend fun refresh() {
        withContext(Dispatchers.IO) {

            var getPickListCall = pickListApiService.getPickLists("DEV001")
            var response = getPickListCall.execute()

            if (!response.isSuccessful) {
                Log.e("TNRMS-MOBILE",
                    "Unable to get picklist from api")
                return@withContext
            }

            var picklist = response.body()

            if (picklist == null) {
                Log.e("TNRMS-MOBILE",
                    "Empty picklist return from get user api endpoint")
                return@withContext
            }

            for (pickListItem in picklist) {
                try {
                    pickListDao.insertAll(
                        PickListEntity(pickListItem.trackingId, pickListItem.pickListNo,
                            pickListItem.cartonNo, pickListItem.barcode,
                            pickListItem.locationCode, pickListItem.wareHouseCode,
                            pickListItem.assignedUserId, pickListItem.status,
                            pickListItem.requestNo, false, null,
                            null, false, null)
                    )
                } catch (e: Exception) {
                    Log.e("TNRMS-MOBILE",
                        "Unable to insert picklist entity to local database", e)
                }
            }
        }
    }
}