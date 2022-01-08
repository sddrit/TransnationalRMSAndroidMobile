package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.*
import java.time.OffsetDateTime

data class PostLocation(
    val barCode: String,
    val locationCode: String,
    val storageType: String,
    val scannedUserName: String,
    val scannedDateTime: String
)

data class LocationResponse(
    val id: Int,
    val code: String,
    val active: Boolean,
    val name: String,
    val type: String
)

data class PallateSummeryItem(
    val scanDate: String,
    val cartonCount: Int,
)

data class PallateDetails(
    val data: ArrayList<PallateDetailsItem>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)

data class PallateDetailsItem(
    var barCode: String,
    var locationCode: String,
    var scannedDateTime: String
)

interface LocationApiService {
    @GET("/v1/api/location/{code}")
    fun getLocation(@Path("code") code: String): Call<LocationResponse>

    @POST("/v1/api/locationItem")
    fun postLocation(@Body locations: List<PostLocation>): Call<Void>

    @GET("/v1/api/locationSummaryByUser/{username}")
    fun getLocationSummery(@Path("username") username: String): Call<ArrayList<PallateSummeryItem>>

    @GET("/v1/api/locationDetailByUser/{username}")
    fun getLocationDetails(
        @Path("username") username: String,
        @Query("dateUtc") dateUtc: String,
        @Query("searchText") searchText: String?,
        @Query("pageIndex") pageIndex: Int,
        @Query("pageSize") pageSize: Int
    ): Call<PallateDetails>
}