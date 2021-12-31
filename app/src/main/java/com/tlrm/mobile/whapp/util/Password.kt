package com.tlrm.mobile.whapp.util

import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Password {

    companion object {
        fun IsPasswordMatch(passwordHash: String, secret: String, password: String): Boolean {
            val sha_HMAC: Mac = Mac.getInstance("HmacSHA512")
            val secretKey = SecretKeySpec(hexStringToByteArray(secret), "HmacSHA512")
            sha_HMAC.init(secretKey)
            var currentPasswordHex = toHexString(sha_HMAC.doFinal(password.toByteArray()))
            return currentPasswordHex == passwordHash
        }

        private fun hexStringToByteArray(s: String): ByteArray? {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                        + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        private fun toHexString(bytes: ByteArray): String? {
            val formatter = Formatter()
            for (b in bytes) {
                formatter.format("%02x", b)
            }
            return formatter.toString()
        }
    }
}