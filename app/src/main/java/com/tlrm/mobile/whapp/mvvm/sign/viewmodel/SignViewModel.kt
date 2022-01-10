package com.tlrm.mobile.whapp.mvvm.sign.viewmodel

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.gcacace.signaturepad.views.SignaturePad
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.database.entities.RequestDetails
import com.tlrm.mobile.whapp.mvvm.main.view.MainActivity
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch

class SignViewModel(
    private val context: Context,
    private val sessionService: SessionService,
    private val requestService: RequestService,
) : ViewModel() {

    var customerName: ObservableField<String>? = null
    var nic: ObservableField<String>? = null
    var designation: ObservableField<String>? = null
    var department: ObservableField<String>? = null;

    var customerNameError: ObservableField<String>? = null
    var nicError: ObservableField<String>? = null

    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    private var requestDetails: RequestDetails? = null

    val requestNumber: MutableLiveData<String> = MutableLiveData<String>()

    init {
    }

    init {
        customerName = ObservableField()
        nic = ObservableField()
        designation = ObservableField()
        department = ObservableField()
        customerNameError = ObservableField()
        nicError = ObservableField()

        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING

            requestDetails = requestService.getCurrentRequest()
            requestNumber.value = requestDetails!!.request.requestNo

            _loadingState.value = LoadingState.LOADED
        }
    }

    fun customerNameWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                customerName!!.set(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {
                if (customerName!!.get().isNullOrEmpty()) {
                    customerNameError!!.set("Customer name is required")
                } else {
                    customerNameError!!.set(null)
                }
            }
        }
    }

    fun nicWatcher(): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                nic!!.set(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {
                if (nic!!.get().isNullOrEmpty()) {
                    nicError!!.set("NIC is required")
                } else {
                    nicError!!.set(null)
                }
            }
        }
    }

    fun complete() {
        if (customerName!!.get().isNullOrEmpty() || nic!!.get().isNullOrEmpty()) {
            _loadingState.value = LoadingState.error("Please fill the required fields")
            return
        }

        val signaturePad =
            (context as AppCompatActivity).findViewById<SignaturePad>(R.id.activity_sign_signature_pad)

        if (signaturePad.isEmpty) {
            _loadingState.value = LoadingState.error("Please enter your signature")
            return
        }

        viewModelScope.launch {
            _loadingState.value = LoadingState.LOADING

            try {
                val user = sessionService.getUser()
                val signature = signaturePad.signatureBitmap

                requestService.sign(
                    context,
                    signature,
                    requestDetails!!.request.requestNo,
                    user.userName,
                    requestDetails!!.request.serialNo,
                    customerName!!.get()!!,
                    nic!!.get()!!,
                    designation!!.get(),
                    department!!.get()
                )

                requestService.clearRequests()

                _loadingState.value = LoadingState.LOADED

                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                (context as AppCompatActivity).finish()

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error(e.message)
            }
        }

    }


}