package com.tlrm.mobile.whapp.util.time

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class LocalServerTimeConverter {

    companion object {
        fun getTime(dateTime: OffsetDateTime): String {
            return dateTime.format(DateTimeFormatter.ISO_DATE_TIME)
        }
    }

}