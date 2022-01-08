package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class CreateRequest(
    val userName: String,
    val requestNumber: String
)

data class CreateRequestResponse(
    val requestNo: String,
    val docketType: String,
    val serialNo: Int,
    val customerCode: String,
    val name: String,
    val contactPerson: String,
    val contactNo: String,
    val docketDetails: ArrayList<String>,
    val emptyDetails: ArrayList<CreateRequestResponseEmptyDetails>
)

data class CreateRequestResponseEmptyDetails(
    val no: Int,
    val fromCarton: Int,
    val toCarton: Int
)

data class GetRequestItemResponse(
    val requestNo: String,
    val name: String,
    val deliveryDate: String,
    val isDigitallySigned: Boolean
)

data class GetRequestResponse(
    val data: ArrayList<GetRequestItemResponse>,
    val pageNumber: Int,
    val totalCount: Int,
    val totalPages: Int
)


interface RequestApiService {
    @GET("/v1/api/request")
    fun getRequests(
        @Query("searchText") searchText: String?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Call<GetRequestResponse>
}