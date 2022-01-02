package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.GET

data class Metadata(
    val fieldDefinitions: ArrayList<MetadataFieldDefinition>,
    val latestAppVersion: String
)

data class MetadataFieldDefinition(
    val code: String,
    val length: Int
)

interface MetadataApiService {
    @GET("/v1/api/metadata")
    fun getUsers(): Call<List<Metadata>>
}