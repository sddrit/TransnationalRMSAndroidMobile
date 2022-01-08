package com.tlrm.mobile.whapp.services

import com.tlrm.mobile.whapp.api.LocationApiService
import com.tlrm.mobile.whapp.api.PallateDetails
import com.tlrm.mobile.whapp.api.PallateSummeryItem
import com.tlrm.mobile.whapp.api.PostLocation
import com.tlrm.mobile.whapp.database.dao.PalleteDao
import com.tlrm.mobile.whapp.database.entities.PallateEntity
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class LocationService(private val locationApiService: LocationApiService,
                      private val palleteDao: PalleteDao) {

    suspend fun pallate(pallete: PallateEntity): Int {
        return withContext(Dispatchers.IO) {
            val id = palleteDao.insert(pallete)
            return@withContext id.toInt()
        }
    }

    suspend fun getPallateInfo(id: Int): PallateEntity {
        return withContext(Dispatchers.IO) {
            return@withContext palleteDao.getPallate(id);
        }
    }

    suspend fun markAsSynced(id: Int, syncedDateTime: String) {
        return withContext(Dispatchers.IO) {
            palleteDao.markAsSynced(id, syncedDateTime)
        }
    }

    suspend fun deleteAllSyncedItem() {
        return withContext(Dispatchers.IO) {
            palleteDao.deleteAllSyncedItem()
        }
    }

    suspend fun getPallateSummery(username: String): ArrayList<PallateSummeryItem>? {
        return withContext(Dispatchers.IO) {
            val call = locationApiService.getLocationSummery(username)
            val response = call.execute()

            if (!response.isSuccessful) {
                throw ServiceException("Unable to get pallate summery details from server")
            }
            return@withContext response.body();
        }
    }

    suspend fun getPallateDetails(
        username: String, date: String, searchText: String? = null,
        pageIndex: Int = 1, pageSize: Int = 100
    ): PallateDetails? {
        return withContext(Dispatchers.IO) {
            val call = locationApiService.getLocationDetails(
                username,
                date,
                searchText,
                pageIndex,
                pageSize
            )
            val response = call.execute()

            if (!response.isSuccessful) {
                throw ServiceException("Unable to get pallate details")
            }

            return@withContext response.body()
        }
    }

    suspend fun syncItem(id: Int): Boolean {
        return withContext(Dispatchers.IO) {

            val pallate = getPallateInfo(id)

            if (pallate.synced) {
                return@withContext true
            }

            val scanedDateTime = pallate.scannedTime!!.format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            val pallates = ArrayList<PostLocation>()
            pallates.add(PostLocation(pallate.cartonNumber, pallate.locationCode,
                pallate.storageType, pallate.scannedUserName, scanedDateTime))

            val call = locationApiService.postLocation(pallates.toList())

            val response = call.execute()

            if (!response.isSuccessful) {
                return@withContext false
            }

            val currentDateTime = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            markAsSynced(pallate.id, currentDateTime)

            return@withContext true
        }

    }

}