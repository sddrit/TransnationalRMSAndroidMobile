package com.tlrm.mobile.whapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tlrm.mobile.whapp.database.entities.PickListEntity

data class PickListDetailsItem(val pickListNo: String, val count: Int, val picked: Int)

@Dao
interface PickListDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg pickList: PickListEntity)

    @Query("UPDATE PickList SET picked = 1, picked_user_id = :pickedUserId, " +
            "picked_date_time = :pickedDateTime " +
            "WHERE carton_no = :cartonNo")
    fun markAsPicked(cartonNo: String, pickedUserId: Int, pickedDateTime: String)

    @Query("UPDATE PickList SET synced = 1, synced_date_time = :syncedDateTime " +
            "WHERE tracking_id = :trackingId")
    fun markAsSynced(trackingId: Int, syncedDateTime: String)

    @Query("SELECT * from PickList where synced = 0")
    fun getUnSyncedPickListItems(): List<PickListEntity>

    @Query("SELECT * FROM PickList where barcode = :cartonNo LIMIT 1")
    fun getPickListItem(cartonNo: String) : PickListEntity

    @Query("SELECT * from PickList where pick_list_no = :pickListNo COLLATE NOCASE")
    fun getPickListItems(pickListNo: String): List<PickListEntity>

    @Query("SELECT * from PickList where pick_list_no = :pickListNo " +
            "AND LOWER(barcode) like '%' || :searchText || '%' COLLATE NOCASE COLLATE NOCASE")
    fun getPickListItems(pickListNo: String, searchText: String): List<PickListEntity>

    @Query("select distinct pick_list_no As pickListNo, " +
            "(SELECT count(*) From PickList pl_ct_tb " +
            "Where pl_ct_tb.pick_list_no = pl_tb.pick_list_no) as count, " +
            "(SELECT count(*) From PickList pl_ct_tb " +
            "Where pl_ct_tb.pick_list_no = pl_tb.pick_list_no and pl_ct_tb.picked = 1) as picked " +
            "from PickList pl_tb " +
            "order by PickListNo desc")
    fun getPickListDetailItems() : List<PickListDetailsItem>

    @Query("select distinct pick_list_no As pickListNo, " +
            "(SELECT count(*) From PickList pl_ct_tb " +
            "Where pl_ct_tb.pick_list_no = pl_tb.pick_list_no) as count, " +
            "(SELECT count(*) From PickList pl_ct_tb " +
            "Where pl_ct_tb.pick_list_no = pl_tb.pick_list_no and pl_ct_tb.picked = 1) as picked " +
            "from PickList pl_tb " +
            "where LOWER(pl_tb.pick_list_no) like '%' || :searchText || '%' COLLATE NOCASE " +
            "order by PickListNo desc ")
    fun getPickListDetailItems(searchText: String) : List<PickListDetailsItem>

    @Query("select distinct pick_list_no As pickListNo, " +
            "(SELECT count(*) From PickList pl_ct_tb " +
            "Where pl_ct_tb.pick_list_no = pl_tb.pick_list_no) as count, " +
            "(SELECT count(*) From PickList pl_ct_tb " +
            "Where pl_ct_tb.pick_list_no = pl_tb.pick_list_no and pl_ct_tb.picked = 1) as picked " +
            "from PickList pl_tb " +
            "where pl_tb.pick_list_no = :pickListNo " +
            "LIMIT 1")
    fun getPickListDetailItem(pickListNo: String) : PickListDetailsItem

}