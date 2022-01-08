package com.tlrm.mobile.whapp.util.api.error

import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException


class ApiErrorUtils {

    companion object {
        fun parseError(response: Response<*>) {

            if (response.code() != 400) {
                throw ServiceException("Unable to execute request")
            }

            val converter: Converter<ResponseBody, ErrorResponse> = ServiceGenerator.retrofit
                .responseBodyConverter(ErrorResponse::class.java, arrayOfNulls<Annotation>(0))
            val error: ErrorResponse = try {
                converter.convert(response.errorBody())
            } catch (e: IOException) {
                ErrorResponse(listOf(ErrorResponseItem("", "Unable to parse error")))
            } ?: throw ServiceException("Unable to execute request")

            throw ServiceException(error.errors[0].message)
        }
    }

}