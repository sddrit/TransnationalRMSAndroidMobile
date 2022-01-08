package com.tlrm.mobile.whapp.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tlrm.mobile.whapp.database.core.DateTimeConverter
import java.time.OffsetDateTime

@Entity(tableName = "Pallate")
data class PallateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val cartonNumber: String,
    val locationCode: String,
    val storageType: String,
    val scannedUserName: String,
    @field:TypeConverters(DateTimeConverter::class)
    val scannedTime: OffsetDateTime?,
    @ColumnInfo(name = "synced") val synced: Boolean,
    @ColumnInfo(name = "synced_date_time")
    @field:TypeConverters(DateTimeConverter::class)
    val syncedDateTime: OffsetDateTime?
)