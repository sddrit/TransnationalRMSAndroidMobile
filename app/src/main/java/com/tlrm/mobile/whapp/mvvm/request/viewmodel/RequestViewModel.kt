package com.tlrm.mobile.whapp.mvvm.request.viewmodel

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.mvvm.request.model.RequestListItem
import com.tlrm.mobile.whapp.mvvm.requestdetails.view.RequestDetailsActivity
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import com.tlrm.mobile.whapp.util.exceptions.ServiceException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import android.widget.Toast

import com.tlrm.mobile.whapp.mvvm.main.view.MainActivity

import android.content.DialogInterface




class RequestViewModel(
    private val context: Context,
    private val sessionService: SessionService,
    private val requestService: RequestService
) : ViewModel() {

    private val TAG = RequestViewModel::class.java.simpleName
    private val PAGE_SIZE = 10;

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<RequestListItem>>(emptyList())

    val data: LiveData<List<RequestListItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    private var searchFor: String = ""
    private var currentPage: Int = 0

    init {
        fetchData(null, 1, PAGE_SIZE)
    }

    fun handleRequest(requestListItem: RequestListItem) {

        if (requestListItem.isDigitallySigned) {
            _loadingState.value = LoadingState.error("Cannot process signed request")
            return
        }

        viewModelScope.launch {
            val user = sessionService.getUser()
            try {
                _loadingState.value = LoadingState.LOADING

                val hasRequest = requestService.hasRequest()

                if(hasRequest) {
                    AlertDialog.Builder(context)
                        .setTitle("Unsigned Request Exits")
                        .setMessage("One of unsigned request exists, Do you want to remove it?")
                        .setIcon(R.drawable.ic_dialog_alert)
                        .setPositiveButton(
                            "DELETE"
                        ) { _, _ ->
                            viewModelScope.launch {
                                requestService.clearRequests()
                                requestService.createRequest(requestListItem.requestNo, user.userName)
                                _loadingState.value = LoadingState.LOADED
                                val requestDetailsIntent =
                                    Intent(context, RequestDetailsActivity::class.java)
                                context.startActivity(requestDetailsIntent)
                            }
                        }
                        .setNegativeButton("CONTINUE") { _, _ ->
                            viewModelScope.launch {
                                _loadingState.value = LoadingState.LOADED
                                val requestDetailsIntent =
                                    Intent(context, RequestDetailsActivity::class.java)
                                context.startActivity(requestDetailsIntent)
                            }
                        }.show()
                } else {
                    requestService.createRequest(requestListItem.requestNo, user.userName)
                    _loadingState.value = LoadingState.LOADED
                    val requestDetailsIntent =
                        Intent(context, RequestDetailsActivity::class.java)
                    context.startActivity(requestDetailsIntent)
                }

            } catch (e: ServiceException) {
                _loadingState.value = LoadingState.error(e.message)
            }
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
            fetchData(searchFor, 1, PAGE_SIZE)
        }
    }

    fun fetchNextPage() {
        viewModelScope.launch {

            if (_loadingState.value == LoadingState.LOADING) {
                return@launch
            }

            _loadingState.value = LoadingState.loading("Loading requests")

            try {

                val requestResponse = requestService.getRequests(searchFor, currentPage + 1, PAGE_SIZE)

                if (requestResponse.totalPages < currentPage + 1) {
                    _loadingState.value = LoadingState.LOADED
                    return@launch
                }

                val requestItems = ArrayList<RequestListItem>(data.value)

                for ((count, item) in requestResponse.data.withIndex()) {
                    val date = OffsetDateTime.parse(item.deliveryDate)
                    requestItems.add(
                        RequestListItem(
                            item.requestNo,
                            item.name,
                            date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            item.isDigitallySigned
                        )
                    )
                }
                _data.postValue(requestItems)
                currentPage += 1

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to load requests")
                Log.e(TAG, "Unable to requests", e)
                return@launch
            }

            _loadingState.value = LoadingState.LOADED
        }
    }

    fun fetchData(searchText: String? = null, page: Int, pageSize: Int) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.loading("Loading requests")

            try {

                val requestResponse = requestService.getRequests(searchText, page, pageSize)

                val requestItems = ArrayList<RequestListItem>()

                for ((count, item) in requestResponse.data.withIndex()) {
                    val date = OffsetDateTime.parse(item.deliveryDate)
                    requestItems.add(
                        RequestListItem(
                            item.requestNo,
                            item.name,
                            date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            item.isDigitallySigned
                        )
                    )
                }
                _data.postValue(requestItems)
                currentPage = page

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to load requests")
                Log.e(TAG, "Unable to requests", e)
                return@launch
            }

            _loadingState.value = LoadingState.LOADED
        }
    }
}