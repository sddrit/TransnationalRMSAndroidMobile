package com.tlrm.mobile.whapp.services

import android.util.Log
import com.tlrm.mobile.whapp.api.MarkAsDeletedPickListItem
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.api.PickListPickRequest
import com.tlrm.mobile.whapp.database.dao.PickListDao
import com.tlrm.mobile.whapp.database.dao.PickListDetailsItem
import com.tlrm.mobile.whapp.database.entities.PickListEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class PickListService(
    private val pickListApiService: PickListApiService,
    private val deviceService: DeviceService,
    private val pickListDao: PickListDao) {

    private val TAG = PickListService::class.java.simpleName

    suspend fun getPickListDetailItems(): List<PickListDetailsItem> {
        return withContext(Dispatchers.IO) {
            return@withContext pickListDao.getPickListDetailItems()
        }
    }

    suspend fun getPickListDetailItems(searchText: String): List<PickListDetailsItem> {
        return withContext(Dispatchers.IO) {
            return@withContext pickListDao.getPickListDetailItems(searchText)
        }
    }

    suspend fun getPickListItems(pickListNo: String): List<PickListEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext pickListDao.getPickListItems(pickListNo)
        }
    }

    suspend fun getPickListItems(pickListNo: String, searchText: String): List<PickListEntity> {
        return withContext(Dispatchers.IO) {
            return@withContext pickListDao.getPickListItems(pickListNo, searchText)
        }
    }

    suspend fun getPickListDetails(pickListNo: String): PickListDetailsItem {
        return withContext(Dispatchers.IO) {
            return@withContext pickListDao.getPickListDetailItem(pickListNo)
        }
    }

    suspend fun markAsPicked(cartonNo: String, pickedUserId: Int, pickedDateTime: String) {
        return withContext(Dispatchers.IO) {
            return@withContext pickListDao.markAsPicked(cartonNo, pickedUserId, pickedDateTime)
        }
    }

    suspend fun deleteCompletedPickList() {
        return withContext(Dispatchers.IO) {
            pickListDao.deleteCompletedPickList()
        }
    }

    suspend fun deletePickList(pickListNo: String) {
        return withContext(Dispatchers.IO) {
            val call = pickListApiService.markAsDeletedFromDevice(MarkAsDeletedPickListItem(pickListNo))
            val response = call.execute()
            if (!response.isSuccessful) {
                throw Error("Unable to mark as delete")
            }
            pickListDao.deletePickList(pickListNo)
        }
    }

    suspend fun syncedItem(cartonNo: String): Boolean {
        return withContext(Dispatchers.IO) {

            var pickListItem = pickListDao.getPickListItem(cartonNo)

            if (pickListItem.synced || !pickListItem.picked) {
                return@withContext true
            }

            var postPickList = ArrayList<PickListPickRequest>()
            var pickedDateTime = pickListItem.pickedDateTime!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            postPickList.add(PickListPickRequest(pickListItem.pickListNo, pickListItem.cartonNo,
                pickedDateTime, pickListItem.pickedUserId!!))

            var call = pickListApiService.postPickLists(postPickList)
            var response = call.execute()

            if(!response.isSuccessful) {
                Log.e(TAG,
                    "Unable to marked as picked the carton no ${pickListItem.barcode}")
                return@withContext false
            }

            val currentDateTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            pickListDao.markAsSynced(pickListItem.trackingId, currentDateTime)

            return@withContext true;
        }
    }

    suspend fun refresh() {
        withContext(Dispatchers.IO) {

            val deviceInfo = deviceService.getDeviceInfo()

            if (deviceInfo == null) {
                Log.e(TAG,
                    "Unable to device info")
                return@withContext
            }

            val getPickListCall = pickListApiService.getPickLists(deviceInfo.code)
            val response = getPickListCall.execute()

            if (!response.isSuccessful) {
                Log.e(TAG,
                    "Unable to get picklist from api")
                return@withContext
            }

            val picklist = response.body()

            if (picklist == null) {
                Log.e(TAG,
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
                    Log.e(TAG,
                        "Unable to insert picklist entity to local database", e)
                }
            }
        }
    }
}