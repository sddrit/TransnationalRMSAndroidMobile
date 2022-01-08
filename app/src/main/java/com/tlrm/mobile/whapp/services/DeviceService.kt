package com.tlrm.mobile.whapp.services

import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceService(
    private val deviceApiService: DeviceApiService,
    private val sessionService: SessionService
) {

    private val TAG = DeviceService::class.java.simpleName

    suspend fun configureDevice(deviceName: String) {
        withContext(Dispatchers.IO) {

            val call = deviceApiService.getDevice(deviceName);

            val response = call.execute()

            if (!response.isSuccessful) {
                throw ServiceException("Unable to get the device info from api")
            }

            val deviceInfo = response.body()
            sessionService.setDevice(deviceInfo!!.code, deviceInfo.description, deviceInfo.active)
        }
    }

    fun getDeviceInfo(): DeviceInfo? {
        return sessionService.getDevice();
    }

}