package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class PickList(val trackingId: Int, val pickListNo: String,
val cartonNo: Int, val barcode: String, val locationCode: String,
val wareHouseCode: String, val assignedUserId: Int, val status: Int,
val requestNo: String)

data class MarkAsDeletedPickListItem(val pickListNo: String)

data class PickListPickRequest(val pickListNo: String, val cartonNo: Int,
                               val pickDateTime: String, val pickedUserId: Int)

interface PickListApiService {
    @GET("/v1/api/picklist/{deviceId}")
    fun getPickLists(@Path("deviceId") deviceId: String): Call<List<PickList>>

    @POST("/v1/api/picklist")
    fun postPickLists(@Body pickLists: List<PickListPickRequest>): Call<Void>

    @POST("/v1/api/pickList/markAsDeletedFromDevice")
    fun markAsDeletedFromDevice(@Body items: MarkAsDeletedPickListItem): Call<Void>
}