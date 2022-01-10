package com.tlrm.mobile.whapp.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

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
    val address: String,
    val contactPerson: String,
    val poNo: String,
    val contactNo: String,
    val department: String,
    val route: String,
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

data class CartonValidationItem(
    val cartonNo: String,
    val canProcess: Boolean,
    val reason: String
)

interface RequestApiService {
    @GET("/v1/api/request")
    fun getRequests(
        @Query("searchText") searchText: String?,
        @Query("pageIndex") page: Int,
        @Query("pageSize") pageSize: Int
    ): Call<GetRequestResponse>

    @POST("/v1/api/request/create-docket")
    fun createDocket(
        @Body request: CreateRequest
    ): Call<CreateRequestResponse>

    @GET("/v1/api/request/validate/{requestNumber}/{cartonNumber}")
    fun validateCarton(
        @Path("requestNumber") requestNumber: String,
        @Path("cartonNumber") cartonNo: String
    ): Call<ArrayList<CartonValidationItem>>

    @Multipart
    @POST("/v1/api/signature")
    fun sign(
        @Part file: MultipartBody.Part,
        @Part("RequestNo") requestNo: String,
        @Part("UserName") username: String,
        @Part("DocketSerialNo") docketSerialNo: Int,
        @Part("CustomerName") customerName: String,
        @Part("CustomerNic") customerNIC: String,
        @Part("CustomerDesignation") CustomerDesignation: String?,
        @Part("CustomerDepartment") CustomerDepartment: String?
    ): Call<Void>
}