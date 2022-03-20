package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class User(val id: Int, val userName: String,
    val userNameMobile: String, val fullName: String,
    val active: Boolean, val passwordHash: String,
    val passwordSalt: String, val roles: ArrayList<String>)

data class LoginHistory(val userId: Int, val loginDate: String, val hostName: String)

interface UserApiService {
    @GET("/v1/api/users")
    fun getUsers(): Call<List<User>>

    @POST("/v1/api/users/addUserLoginHistory")
    fun addUserLoginHistory(@Body loginHistory: LoginHistory): Call<Void>
}