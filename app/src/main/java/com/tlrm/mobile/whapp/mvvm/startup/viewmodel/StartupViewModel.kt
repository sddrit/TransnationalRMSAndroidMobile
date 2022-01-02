package com.tlrm.mobile.whapp.mvvm.startup.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.mvvm.startup.view.StartupActivity
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.UserService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch

class StartupViewModel(
    private val userService: UserService,
    private val deviceService: DeviceService
) : ViewModel() {

    private val TAG = StartupViewModel::class.java.simpleName
    private val _loadingState = MutableLiveData<LoadingState>()

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {

            try {
                _loadingState.value = LoadingState.loading("Configure device configurations")
                deviceService.configureDevice()
                _loadingState.value =
                    LoadingState.loading("Successfully configure the device configurations")
            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to configure the device configurations")
                Log.e(TAG, "Unable to configure the device configurations", e)
            }

            val deviceInfo = deviceService.getDeviceInfo()

            if (deviceInfo == null) {
                _loadingState.value = LoadingState.error(
                    "Unable to get device configurations " +
                            "Please close app and try again or contact administrator"
                )
                return@launch
            }

            if (!deviceInfo.active) {
                _loadingState.value = LoadingState.error("Device is disabled, Please contact administrator")
                return@launch
            }


            try {
                _loadingState.value = LoadingState.loading("Syncing user data from server")
                userService.refresh()
            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to sync the user data from server")
            }

            _loadingState.value = LoadingState.LOADED
        }
    }

}