package com.tlrm.mobile.whapp.api

import retrofit2.Call
import retrofit2.http.GET

data class User(val id: Int, val userName: String,
    val userNameMobile: String, val fullName: String,
    val active: Boolean, val passwordHash: String,
    val passwordSalt: String, val roles: ArrayList<String>)

interface UserApiService {
    @GET("/v1/api/users")
    fun getUsers(): Call<List<User>>
}