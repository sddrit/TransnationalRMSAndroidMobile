package com.tlrm.mobile.whapp.mvvm.pallatedetails.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.mvvm.pallatedetails.model.PallateDetailsSummeryListItem
import com.tlrm.mobile.whapp.services.LocationService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PallateDetailsViewModel(
    private val context: Context,
    private val sessionService: SessionService,
    private val locationService: LocationService
) : ViewModel() {

    private val TAG = PallateDetailsViewModel::class.java.simpleName

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<PallateDetailsSummeryListItem>>(emptyList())

    val data: LiveData<List<PallateDetailsSummeryListItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    init {
        fetchData()
    }

    fun fetchData(searchText: String? = null) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.loading("Loading pallate details summery")

            val user = sessionService.getUser()

            try {

                val pallateDetailsSummeryItems = locationService.getPallateSummery(user.userName)

                if (pallateDetailsSummeryItems == null) {
                    _loadingState.value = LoadingState.error(
                        "Unable to load pallate details " +
                                "summery from server"
                    )
                    return@launch
                }

                val pallateDetailsSummeryListItems = ArrayList<PallateDetailsSummeryListItem>()

                for ((count, item) in pallateDetailsSummeryItems.withIndex()) {
                    val date = LocalDateTime.parse(item.scanDate.replace('T', ' '),
                        DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"))
                    pallateDetailsSummeryListItems.add(
                        PallateDetailsSummeryListItem(
                            count,
                            date.format(DateTimeFormatter.ISO_LOCAL_DATE), item.cartonCount
                        )
                    )
                }

                _data.postValue(pallateDetailsSummeryListItems)

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to load pallate details summery")
                Log.e(TAG, "Unable to load pallate details summery", e)
                return@launch
            }

            _loadingState.value = LoadingState.LOADED
        }
    }
}