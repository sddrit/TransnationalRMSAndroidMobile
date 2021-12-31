package com.tlrm.mobile.whapp.mvvm.main.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tlrm.mobile.whapp.mvvm.login.view.LoginActivity
import com.tlrm.mobile.whapp.services.SessionService
import java.util.*
import android.app.Activity
import com.tlrm.mobile.whapp.mvvm.picklist.view.PickListActivity


class MainViewModel(private val context: Context,
                    private val sessionService: SessionService): ViewModel() {

    val fullName: MutableLiveData<String> = MutableLiveData<String>();
    var greeting: MutableLiveData<String> = MutableLiveData<String>();

    init {
        val user = sessionService.getUser()
        fullName.value = user.fullName
        greeting.value = getGreetingMessage()
    }

    fun pickList() {
        val pickListIntent = Intent(context, PickListActivity::class.java)
        context.startActivity(pickListIntent)
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