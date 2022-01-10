package com.tlrm.mobile.whapp.database.entities

import androidx.room.*

@Entity(tableName = "Requests")
data class RequestEntity(
    @PrimaryKey() @ColumnInfo(name = "request_no") val requestNo: String,
    @ColumnInfo(name = "docket_type") val docketType: String,
    @ColumnInfo(name = "serial_no") val serialNo: Int,
    @ColumnInfo(name = "customer_code") val customerCode: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "contact_person") val contactPerson: String,
    @ColumnInfo(name = "po_no") val poNo: String,
    @ColumnInfo(name = "contact_no") val contactNo: String,
    @ColumnInfo(name = "department") val department: String,
    @ColumnInfo(name = "route") val route: String
)

@Entity(tableName = "RequestDocketItem")
data class RequestDocketItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "request_no") val requestNo: String,
    @ColumnInfo(name = "carton_no") val cartonNo: String,
    @ColumnInfo(name = "scanned") val scanned: Boolean
)

@Entity(tableName = "RequestEmptyItem")
data class RequestEmptyItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "request_no") val requestNo: String,
    @ColumnInfo(name = "from_carton_no") val fromCartonNo: String,
    @ColumnInfo(name = "to_carton_no") val toCartonNo: String,
    @ColumnInfo(name = "scanned") val scanned: Boolean
)

data class RequestDetails(
    @Embedded val request: RequestEntity,
    @Relation(
        parentColumn = "request_no",
        entityColumn = "request_no"
    )
    val requestDocketItems: List<RequestDocketItemEntity>,
    @Relation(
        parentColumn = "request_no",
        entityColumn = "request_no"
    )
    val requestEmptyItems: List<RequestEmptyItemEntity>
)