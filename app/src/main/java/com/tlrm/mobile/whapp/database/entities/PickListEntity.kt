package com.tlrm.mobile.whapp.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tlrm.mobile.whapp.database.core.DateTimeConverter
import java.time.OffsetDateTime

@Entity(tableName = "PickList")
data class PickListEntity(
    @PrimaryKey @ColumnInfo(name = "tracking_id") val trackingId: Int,
    @ColumnInfo(name = "pick_list_no") val pickListNo: String,
    @ColumnInfo(name = "carton_no") val cartonNo: Int,
    @ColumnInfo(name = "barcode") val barcode: String,
    @ColumnInfo(name = "location_code") val locationCode: String,
    @ColumnInfo(name = "warehouse_code") val wareHouseCode: String,
    @ColumnInfo(name = "assigned_user_id") val assignedUserId: Int,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "request_no") val requestNo: String,
    @ColumnInfo(name = "picked") val picked: Boolean,
    @ColumnInfo(name = "picked_user_id") val pickedUserId: Int?,
    @ColumnInfo(name = "picked_date_time")
    @field:TypeConverters(DateTimeConverter::class)
    val pickedDateTime: OffsetDateTime?,
    @ColumnInfo(name = "synced") val synced: Boolean,
    @ColumnInfo(name = "synced_date_time")
    @field:TypeConverters(DateTimeConverter::class)
    val syncedDateTime: OffsetDateTime?
)