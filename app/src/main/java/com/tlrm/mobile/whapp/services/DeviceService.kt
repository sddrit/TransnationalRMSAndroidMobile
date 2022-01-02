package com.tlrm.mobile.whapp.services

import android.os.Build
import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.mvvm.picklist.viewmodel.PickListViewModel
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceService(
    private val deviceApiService: DeviceApiService,
    private val sessionService: SessionService
) {

    private val TAG = DeviceService::class.java.simpleName

    suspend fun configureDevice() {
        withContext(Dispatchers.IO) {
            val call = deviceApiService.getDevice(Build.DEVICE);

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