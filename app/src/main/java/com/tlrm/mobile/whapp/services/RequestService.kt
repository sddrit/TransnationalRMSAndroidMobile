package com.tlrm.mobile.whapp.services

import com.tlrm.mobile.whapp.api.GetRequestResponse
import com.tlrm.mobile.whapp.api.RequestApiService
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestService(private val requestApiService: RequestApiService) {

    suspend fun getRequests(
        searchText: String?,
        page: Int,
        pageSize: Int
    ): GetRequestResponse {
        return withContext(Dispatchers.IO) {
            val call = requestApiService.getRequests(searchText, page, pageSize)
            val response = call.execute()
            if (!response.isSuccessful) {
                throw ServiceException("Unable to get requests")
            }
            return@withContext response.body()!!
        }
    }

}