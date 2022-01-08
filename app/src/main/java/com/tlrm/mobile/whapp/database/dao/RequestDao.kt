package com.tlrm.mobile.whapp.database.dao;

import androidx.room.Dao;
import androidx.room.Insert
import androidx.room.Query;
import androidx.room.Transaction;

import com.tlrm.mobile.whapp.database.entities.RequestDetails;
import com.tlrm.mobile.whapp.database.entities.RequestDocketItemEntity
import com.tlrm.mobile.whapp.database.entities.RequestEmptyItemEntity
import com.tlrm.mobile.whapp.database.entities.RequestEntity

@Dao
interface RequestDao {
    @Transaction
    @Query("SELECT * FROM Requests LIMIT 1")
    fun getRequest(): RequestDetails

    @Insert
    fun insert(request: RequestEntity)

    @Insert
    fun insertDocketItems(items: List<RequestDocketItemEntity>)

    @Insert
    fun insertEmptyItems(items: List<RequestEmptyItemEntity>)

    @Transaction
    fun insertRequest(request: RequestDetails) {
        insert(request.request)

        if (request.requestDocketItems.isNotEmpty()) {
            insertDocketItems(request.requestDocketItems)
        }

        if (request.requestEmptyItems.isNotEmpty()) {
            insertEmptyItems(request.requestEmptyItems)
        }
    }

}
