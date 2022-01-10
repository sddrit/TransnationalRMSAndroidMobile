package com.tlrm.mobile.whapp.mvvm.requestscan.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.tlrm.mobile.whapp.database.entities.RequestDetails
import com.tlrm.mobile.whapp.mvvm.requestdetails.model.RequestDetailsItem
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch
import java.lang.Exception

class RequestScanViewModel(
    private val context: Context,
    private val sessionService: SessionService,
    private val requestService: RequestService
) : ViewModel() {

    private val TAG = RequestScanViewModel::class.java.simpleName

    val count: MutableLiveData<String> = MutableLiveData<String>()
    var pending: MutableLiveData<String> = MutableLiveData<String>()

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<RequestDetailsItem>>(emptyList())

    private var requestDetails: RequestDetails? = null

    val data: LiveData<List<RequestDetailsItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    val requestNumber: MutableLiveData<String> = MutableLiveData<String>()

    var previousBarcode: String? = null

    init {
        viewModelScope.launch {

            _loadingState.value = LoadingState.LOADING

            setRequestDetails()

            _data.observe((context as AppCompatActivity), { requestDetails ->
                count.value = requestDetails.count().toString()
                pending.value = requestDetails.filter { !it.scanned }.count().toString()
            })

            _loadingState.value = LoadingState.LOADED
        }
    }

    private suspend fun setRequestDetails() {
        requestDetails = requestService.getCurrentRequest()
        requestNumber.value = requestDetails!!.request.requestNo

        val requestDetailsItems = getRequestDetailItems()
        _data.postValue(requestDetailsItems)

    }

    fun scan(cartonNumber: String) {
        viewModelScope.launch {

            //RETRIEVAL

            if (requestDetails!!.request.docketType.uppercase() == "RETRIEVAL DOCKET") {
                val requestDetailsItems = _data.value;
                val requestDetailsItem =
                    requestDetailsItems!!.filter { it.cartonNumber!!.uppercase() == cartonNumber.uppercase() }
                if (requestDetailsItem.isEmpty()) {
                    _loadingState.value = LoadingState.error("Invalid carton number")
                    return@launch;
                }
                requestService.markAsScan(cartonNumber)
                requestDetailsItem[0].scanned = true
                _data.postValue(requestDetailsItems!!)
            }

            //EMPTY

            if (requestDetails!!.request.docketType.uppercase() == "EMPTY DOCKET") {
                if (previousBarcode == null) {
                    previousBarcode = cartonNumber
                    return@launch
                }
                val requestDetailsItems = _data.value;
                val requestDetailsItem = requestDetailsItems!!.filter {
                    (it.fromCartonNumber!!.uppercase() == cartonNumber.uppercase() && it.toCartonNumber!!.uppercase() == previousBarcode!!.uppercase()) ||
                            (it.toCartonNumber!!.uppercase() == cartonNumber.uppercase() && it.fromCartonNumber!!.uppercase() == previousBarcode!!.uppercase())
                }
                if (requestDetailsItem.isEmpty()) {
                    _loadingState.value = LoadingState.error("Invalid carton range")
                    previousBarcode = null
                    return@launch;
                }
                requestService.markAsScan(requestDetailsItem[0].fromCartonNumber!!, requestDetailsItem[0].toCartonNumber!!)
                requestDetailsItem[0].scanned = true
                _data.postValue(requestDetailsItems!!)
            }

            //COLLECTION

            if (requestDetails!!.request.docketType.uppercase() == "COLLECTION DOCKET") {
                val requestDetailsItems = _data.value;
                val requestDetailsItem =
                    requestDetailsItems!!.filter { it.cartonNumber!!.uppercase().contains(cartonNumber.uppercase()) }
                if (requestDetailsItem.isEmpty()) {
                    try {
                        requestService.insertCarton(
                            requestDetails!!.request.requestNo,
                            cartonNumber
                        )
                        setRequestDetails()
                    } catch (e: Exception) {
                        _loadingState.value = LoadingState.error(e.message)
                    }
                    return@launch;
                }
                requestService.markAsScan(requestDetailsItem[0].cartonNumber!!)
                requestDetailsItem[0].scanned = true
                _data.postValue(requestDetailsItems!!)
            }
        }
    }

    fun complete() {
        (context as AppCompatActivity).onBackPressed()
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