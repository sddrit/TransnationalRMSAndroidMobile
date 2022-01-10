package com.tlrm.mobile.whapp.mvvm.requestdetails.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.database.entities.RequestDetails
import com.tlrm.mobile.whapp.mvvm.requestdetails.model.RequestDetailsItem
import com.tlrm.mobile.whapp.mvvm.requestscan.view.RequestScanActivity
import com.tlrm.mobile.whapp.mvvm.sign.view.SignActivity
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RequestDetailsViewModel(
    private val context: Context,
    private val sessionService: SessionService,
    private val requestService: RequestService
) : ViewModel() {

    private val TAG = RequestDetailsViewModel::class.java.simpleName

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<RequestDetailsItem>>(emptyList())

    private var requestDetails: RequestDetails? = null

    val data: LiveData<List<RequestDetailsItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    private var searchFor: String = ""

    val requestNumber: MutableLiveData<String> = MutableLiveData<String>()
    val canSign: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    init {
       init()
    }

    fun init() {
        viewModelScope.launch {

            _loadingState.value = LoadingState.LOADING

            requestDetails = requestService.getCurrentRequest()
            requestNumber.value = requestDetails!!.request.requestNo

            val requestDetailsItems = getRequestDetailItems()
            _data.postValue(requestDetailsItems)

            canSign.value = requestDetailsItems.all { it.scanned }

            _loadingState.value = LoadingState.LOADED
        }
    }

    fun onSearchTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val searchText = s.toString().trim()
        if (searchText == searchFor)
            return

        searchFor = searchText

        viewModelScope.launch {
            delay(500)  //debounce timeOut
            if (searchText != searchFor)
                return@launch
            fetchData(searchText)
        }
    }

    fun fetchData(searchText: String?) {

        val requestDetailsItems = getRequestDetailItems()

        if (searchText == null) {
            _data.postValue(requestDetailsItems)
        }

        val result = requestDetailsItems.filter { (it.cartonNumber != null && it.cartonNumber!!.uppercase().contains(searchText!!.uppercase()))
                || it.fromCartonNumber != null && it.fromCartonNumber!!.uppercase().contains(searchText!!.uppercase())
                || it.toCartonNumber != null && it.toCartonNumber!!.uppercase().contains(searchText!!.uppercase())}

        _data.postValue(result)
    }

    fun scan() {
        val intent = Intent(context, RequestScanActivity::class.java)
        context.startActivity(intent)
    }

    fun sign() {
        val intent = Intent(context, SignActivity::class.java)
        context.startActivity(intent)
    }

    private fun getRequestDetailItems(): ArrayList<RequestDetailsItem> {

        val requestDetailsItems = ArrayList<RequestDetailsItem>()

        if (requestDetails!!.request.docketType.uppercase() == "RETRIEVAL DOCKET") {
            for (item in requestDetails!!.requestDocketItems) {
                requestDetailsItems.add(
                    RequestDetailsItem(
                        item.cartonNo,
                        null,
                        null,
                        false,
                        item.scanned
                    )
                )
            }
        }

        if (requestDetails!!.request.docketType.uppercase() == "EMPTY DOCKET") {
            for (item in requestDetails!!.requestEmptyItems) {
                requestDetailsItems.add(
                    RequestDetailsItem(
                        null,
                        item.fromCartonNo, item.toCartonNo,
                        true,
                        item.scanned
                    )
                )
            }
        }

        if (requestDetails!!.request.docketType.uppercase() == "COLLECTION DOCKET") {
            for (item in requestDetails!!.requestDocketItems) {
                requestDetailsItems.add(
                    RequestDetailsItem(
                        item.cartonNo,
                        null,
                        null,
                        false,
                        item.scanned
                    )
                )
            }
        }

        return requestDetailsItems
    }

}