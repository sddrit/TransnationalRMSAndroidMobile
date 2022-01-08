package com.tlrm.mobile.whapp.services

import com.tlrm.mobile.whapp.api.CreateRequest
import com.tlrm.mobile.whapp.api.GetRequestResponse
import com.tlrm.mobile.whapp.api.RequestApiService
import com.tlrm.mobile.whapp.database.dao.RequestDao
import com.tlrm.mobile.whapp.database.entities.RequestDetails
import com.tlrm.mobile.whapp.database.entities.RequestDocketItemEntity
import com.tlrm.mobile.whapp.database.entities.RequestEmptyItemEntity
import com.tlrm.mobile.whapp.database.entities.RequestEntity
import com.tlrm.mobile.whapp.util.api.error.ApiErrorUtils
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestService(
    private val requestDao: RequestDao,
    private val requestApiService: RequestApiService
) {

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

    suspend fun createRequest(requestNo: String, username: String) {
        return withContext(Dispatchers.IO) {
            val call = requestApiService.createDocket(CreateRequest(username, requestNo))
            val response = call.execute()

            if (!response.isSuccessful) {
                ApiErrorUtils.parseError(response)
            }

            val request = response.body()

            requestDao.insertRequest(RequestDetails(
                RequestEntity(
                    request!!.requestNo,
                    request.docketType,
                    request.serialNo,
                    request.customerCode,
                    request.name,
                    request.address,
                    request.contactPerson,
                    request.poNo,
                    request.contactNo,
                    request.department,
                    request.route
                ),
                request.docketDetails.map {
                    RequestDocketItemEntity(
                        0,
                        request.requestNo,
                        it,
                        false
                    )
                },
                request.emptyDetails.map {
                    RequestEmptyItemEntity(
                        0,
                        request.requestNo,
                        it.fromCarton.toString(),
                        it.toCarton.toString()
                    )
                }
            ))
        }
    }

}