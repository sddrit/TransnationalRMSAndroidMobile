package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

data class DeviceInfo(val code: String, val description: String, val active: Boolean)

interface DeviceApiService {
    @GET("/v1/api/mobileDevice/{serialNumber}")
    fun getDevice(@Path("serialNumber") serialNumber: String): Call<DeviceInfo>
}