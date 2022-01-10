package com.tlrm.mobile.whapp.mvvm.requestdetails.model

data class RequestDetailsItem (
    var cartonNumber: String?,
    var fromCartonNumber: String?,
    var toCartonNumber: String?,
    var isEmptyRange: Boolean,
    var scanned: Boolean
)