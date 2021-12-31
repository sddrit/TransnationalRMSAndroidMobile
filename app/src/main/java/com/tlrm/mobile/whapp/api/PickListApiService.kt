package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class PickList(val trackingId: Int, val pickListNo: String,
val cartonNo: Int, val barcode: String, val locationCode: String,
val wareHouseCode: String, val assignedUserId: Int, val status: Int,
val requestNo: String)

interface PickListApiService {
    @GET("/v1/api/picklist/{deviceId}")
    fun getPickLists(@Path("deviceId") deviceId: String): Call<List<PickList>>
}