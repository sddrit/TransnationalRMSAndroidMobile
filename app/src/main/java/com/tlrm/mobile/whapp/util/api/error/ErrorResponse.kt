package com.tlrm.mobile.whapp.util.api.error

class ErrorResponse(val errors: List<ErrorResponseItem>) {
}

class ErrorResponseItem(
    val code: String,
    val message: String
)