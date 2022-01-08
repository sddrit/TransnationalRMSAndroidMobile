package com.tlrm.mobile.whapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tlrm.mobile.whapp.database.entities.PallateEntity
import com.tlrm.mobile.whapp.database.entities.PickListEntity

@Dao
interface PalleteDao {
    @Insert
    fun insert(pallate: PallateEntity): Long

    @Query("SELECT * FROM Pallate where id = :id LIMIT 1")
    fun getPallate(id: Int) : PallateEntity

    @Query("UPDATE Pallate SET synced = 1, synced_date_time = :syncedDateTime " +
            "WHERE id = :id")
    fun markAsSynced(id: Int, syncedDateTime: String)

    @Query("DELETE FROM Pallate WHERE synced = 1")
    fun deleteAllSyncedItem()
}