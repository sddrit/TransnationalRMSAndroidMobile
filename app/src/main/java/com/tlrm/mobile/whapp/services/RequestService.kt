package com.tlrm.mobile.whapp.services

import android.content.Context
import android.graphics.Bitmap
import com.tlrm.mobile.whapp.api.CartonValidationItem
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*


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

    suspend fun getCurrentRequest(): RequestDetails {
        return withContext(Dispatchers.IO) {
            return@withContext requestDao.getRequest()
        }
    }

    suspend fun clearRequests() {
        withContext(Dispatchers.IO) {
            requestDao.clearRequest()
        }
    }

    suspend fun markAsScan(cartonNumber: String) {
        withContext(Dispatchers.IO) {
            requestDao.markAsScan(cartonNumber)
        }
    }

    suspend fun markAsScan(fromCartonNumber: String, toCartonNumber: String) {
        withContext(Dispatchers.IO) {
            requestDao.markAsScan(fromCartonNumber, toCartonNumber)
        }
    }

    suspend fun insertCarton(requestNo: String, cartonNumber: String) {
        withContext(Dispatchers.IO) {
            val validationResult = validateCarton(requestNo, cartonNumber)
            if (!validationResult.canProcess) {
                throw ServiceException(validationResult.reason)
            }
            val docketItems = ArrayList<RequestDocketItemEntity>()
            docketItems.add(RequestDocketItemEntity(0, requestNo, cartonNumber, true))
            requestDao.insertDocketItems(docketItems)
        }
    }

    suspend fun hasRequest() : Boolean {
        return withContext(Dispatchers.IO) {
            return@withContext requestDao.hasRequest()
        }
    }

    suspend fun validateCarton(requestNo: String, cartonNumber: String): CartonValidationItem {
        return withContext(Dispatchers.IO) {

            val call = requestApiService.validateCarton(requestNo, cartonNumber)
            val response = call.execute()

            if (!response.isSuccessful) {
                ApiErrorUtils.parseError(response)
            }

            return@withContext response.body()!![0]
        }
    }

    suspend fun sign(
        context: Context,
        bitmap: Bitmap,
        requestNo: String,
        username: String,
        docketSerialNo: Int,
        customerName: String,
        customerNIC: String,
        customerDesignation: String?,
        customerDepartment: String?
    ) {
        withContext(Dispatchers.IO) {
            try {

                val imageFile = convertBitmapToFile(context, "signature.jpg", bitmap)

                val requestFile: RequestBody =
                    imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                val body: MultipartBody.Part =
                    MultipartBody.Part.createFormData("File", "signature.jpg", requestFile)

                val call = requestApiService.sign(
                    body,
                    requestNo,
                    username,
                    docketSerialNo,
                    customerName,
                    customerNIC,
                    customerDesignation,
                    customerDepartment
                )

                val response = call.execute()

                if (!response.isSuccessful) {
                    ApiErrorUtils.parseError(response)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
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
                        it.toCarton.toString(),
                        false
                    )
                }
            ))
        }
    }

    fun convertBitmapToFile(context: Context, fileName: String, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(context.cacheDir, fileName)
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos)
        val bitMapData = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            fos?.write(bitMapData)
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

}