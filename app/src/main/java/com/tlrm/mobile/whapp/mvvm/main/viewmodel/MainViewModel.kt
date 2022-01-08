package com.tlrm.mobile.whapp.mvvm.main.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tlrm.mobile.whapp.BuildConfig
import com.tlrm.mobile.whapp.mvvm.login.view.LoginActivity
import com.tlrm.mobile.whapp.mvvm.pallate.view.PallateActivity
import com.tlrm.mobile.whapp.mvvm.pallatedetails.view.PallateDetailsActivity
import com.tlrm.mobile.whapp.mvvm.picklist.view.PickListActivity
import com.tlrm.mobile.whapp.mvvm.request.view.RequestActivity
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.SessionService
import java.util.*


class MainViewModel(private val context: Context,
                    private val deviceService: DeviceService,
                    private val sessionService: SessionService): ViewModel() {

    val fullName: MutableLiveData<String> = MutableLiveData<String>();
    val greeting: MutableLiveData<String> = MutableLiveData<String>();
    val deviceName: MutableLiveData<String> = MutableLiveData<String>();
    val version: MutableLiveData<String> = MutableLiveData<String>()

    init {
        val user = sessionService.getUser()
        var deviceInfo = deviceService.getDeviceInfo()
        fullName.value = user.fullName
        greeting.value = getGreetingMessage()
        deviceName.value = "DEVICE : ${deviceInfo!!.description}".uppercase()
        version.value = "VERSION : ${BuildConfig.VERSION_NAME}".uppercase()
    }

    fun pickList() {
        val pickListIntent = Intent(context, PickListActivity::class.java)
        context.startActivity(pickListIntent)
    }

    fun pallate() {
        val pallateIntent = Intent(context, PallateActivity::class.java)
        context.startActivity(pallateIntent)
    }

    fun pallateDetails() {
        val pallateDetailsIntent = Intent(context, PallateDetailsActivity::class.java)
        context.startActivity(pallateDetailsIntent)
    }

    fun request() {
        val requestIntent = Intent(context, RequestActivity::class.java)
        context.startActivity(requestIntent)
    }

    fun logOff() {
        sessionService.clearUser()
        val loginIntent = Intent(context, LoginActivity::class.java)
        context.startActivity(loginIntent)
        (context as Activity).finish()
    }

    private fun getGreetingMessage(): String{
        val c = Calendar.getInstance()
        val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

        return when (timeOfDay) {
            in 0..11 -> "Good Morning!"
            in 12..15 -> "Good Afternoon!"
            in 16..23 -> "Good Evening!"
            else -> "Hello"
        }
    }

}