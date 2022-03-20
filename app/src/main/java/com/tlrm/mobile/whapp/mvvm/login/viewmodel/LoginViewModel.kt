package com.tlrm.mobile.whapp.mvvm.login.viewmodel

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.services.UserService
import com.tlrm.mobile.whapp.util.LoadingState
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import com.tlrm.mobile.whapp.workers.LoginHistorySyncWorker
import com.tlrm.mobile.whapp.workers.PallateSyncWorker
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class LoginViewModel(private val context: Context,
                     private val sessionService: SessionService,
                     private val userService: UserService) : ViewModel() {

    private val _loadingState = MutableLiveData<LoadingState>()

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    var username: ObservableField<String>? = null
    var password: ObservableField<String>? = null
    var usernameError: ObservableField<String>? = null
    var passwordError: ObservableField<String>? = null

    init {
        username = ObservableField()
        password = ObservableField()
        usernameError = ObservableField()
        passwordError = ObservableField()
        username!!.set("078")
        password!!.set("1234Qwer@")
    }

    fun userNameWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                username!!.set(charSequence.toString())
            }
            override fun afterTextChanged(editable: Editable) {
                if (username!!.get().isNullOrEmpty()) {
                    usernameError!!.set("Username is required")
                }else {
                    usernameError!!.set(null)
                }
            }
        }
    }

    fun passwordWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                password!!.set(charSequence.toString())
            }
            override fun afterTextChanged(editable: Editable) {
                if (password!!.get().isNullOrEmpty()) {
                    passwordError!!.set("Password is required")
                }else {
                    passwordError!!.set(null)
                }
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING
            try {
                val user = userService.login(username!!.get()!!, password!!.get()!!)
                sessionService.setUser(user)

                val deviceInfo = sessionService.getDevice();

                val builder: Constraints.Builder = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)

                val currentDateTime = OffsetDateTime.now(ZoneOffset.UTC)

                val data: Data.Builder = Data.Builder()
                data.putInt("userId", user.id)
                data.putString("loginDate", currentDateTime.format(
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                data.putString("hostName", deviceInfo!!.code)

                val syncWorkRequest = OneTimeWorkRequest.Builder(LoginHistorySyncWorker::class.java)
                    .addTag("login-sync")
                    .setInputData(data.build())
                    .setConstraints(builder.build())
                    .build()

                WorkManager.getInstance(context).enqueue(syncWorkRequest)

                _loadingState.value = LoadingState.LOADED
            }catch (e: ServiceException) {
                _loadingState.value = LoadingState.error(e.message)
            }catch (e: Exception) {
                _loadingState.value = LoadingState.error(e.message)
            }
        }
    }
}