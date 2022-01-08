package com.tlrm.mobile.whapp.mvvm.pallateditems.view

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.mvvm.pallateditems.model.PallateItem
import com.tlrm.mobile.whapp.mvvm.pallateditems.viewmodel.PallatedItemsActivity
import com.tlrm.mobile.whapp.services.LocationService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class PallatedItemsViewModel(
    private val context: Context,
    private val locationService: LocationService
) : ViewModel() {

    private val TAG = PallatedItemsActivity::class.java.simpleName
    private val PAGE_SIZE = 10;

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<PallateItem>>(emptyList())

    val data: LiveData<List<PallateItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    private var searchFor: String = ""
    private var currentPage: Int = 0

    val date = MutableLiveData<String>()

    init {
        val extras = (context as Activity).intent.extras
        date.value = extras!!.getString("date")!!

        fetchData(null, 1, PAGE_SIZE)
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

            _loadingState.value = LoadingState.loading("Loading pallate details")

            val extras = (context as Activity).intent.extras

            if (extras == null) {
                _loadingState.value = LoadingState.error("Unable to get pallate details")
                return@launch
            }

            val date = extras.getString("date")!!
            val username = extras.getString("username")

            try {

                val pallateDetails = locationService.getPallateDetails(
                    username!!, date, searchFor,
                    currentPage + 1, PAGE_SIZE
                )

                if (pallateDetails!!.totalPages < currentPage + 1) {
                    _loadingState.value = LoadingState.LOADED
                    return@launch
                }

                val items = ArrayList<PallateItem>(data.value)

                for ((count, item) in pallateDetails!!.data.withIndex()) {
                    val date = LocalDateTime.parse(
                        item.scannedDateTime
                    )
                    items.add(
                        PallateItem(
                            item.barCode,
                            item.locationCode,
                            date.atOffset(ZoneOffset.UTC).toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )
                    )
                }

                _data.postValue(items)
                currentPage += 1

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to load pallate details")
                Log.e(TAG, "Unable to load pallate details", e)
                return@launch
            }

            _loadingState.value = LoadingState.LOADED
        }
    }

    fun fetchData(searchText: String? = null, page: Int, pageSize: Int) {
        viewModelScope.launch {
            _loadingState.value = LoadingState.loading("Loading pallate details")

            val extras = (context as Activity).intent.extras

            if (extras == null) {
                _loadingState.value = LoadingState.error("Unable to get pallate details")
                return@launch
            }

            val date = extras.getString("date")!!
            val username = extras.getString("username")

            try {

                val pallateDetails = locationService.getPallateDetails(
                    username!!, date, searchFor,
                    currentPage + 1, PAGE_SIZE
                )

                val items = ArrayList<PallateItem>()

                for ((count, item) in pallateDetails!!.data.withIndex()) {
                    val date = LocalDateTime.parse(
                        item.scannedDateTime
                    )
                    items.add(
                        PallateItem(
                            item.barCode,
                            item.locationCode,
                            date.atOffset(ZoneOffset.UTC).toLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        )
                    )
                }
                _data.postValue(items)
                currentPage = page

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to load pallate details")
                Log.e(TAG, "Unable to load pallate details", e)
                return@launch
            }

            _loadingState.value = LoadingState.LOADED
        }
    }

}