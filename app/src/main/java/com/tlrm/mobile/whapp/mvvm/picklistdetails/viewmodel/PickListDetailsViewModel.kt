package com.tlrm.mobile.whapp.mvvm.picklistdetails.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.tlrm.mobile.whapp.mvvm.picklistscan.view.PickListScanActivity
import com.tlrm.mobile.whapp.mvvm.picklistdetails.model.PickListDetailsItem
import com.tlrm.mobile.whapp.mvvm.picklistscan.viewmodel.PickListScanViewModel
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import com.tlrm.mobile.whapp.workers.PickItemSyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class PickListDetailsViewModel(
    private val context: Context,
    private val sessionService: SessionService,
    private val pickListService: PickListService
): ViewModel() {

    private val TAG = PickListDetailsViewModel::class.java.simpleName

    val pickListNo: MutableLiveData<String> = MutableLiveData<String>();

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<PickListDetailsItem>>(emptyList())

    private var searchFor: String = ""

    val data: LiveData<List<PickListDetailsItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    init {
        fetchData()
    }

    fun scan() {
        val extras = (context as Activity).intent.extras ?: return
        val pickListNumber = extras.getString("pick_list_name")!!
        val intent = Intent(context, PickListScanActivity::class.java)
        intent.putExtra("pick_list_name", pickListNumber)
        context.startActivity(intent)
    }

    fun scan(barcode: String) {
        viewModelScope.launch {

            val user = sessionService.getUser()
            val currentDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            try {

                pickListService.markAsPicked(barcode, user.id, currentDateTime)

                val builder: Constraints.Builder = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)

                val data: Data.Builder = Data.Builder()
                data.putString("carton_no", barcode)

                val syncWorkRequest = OneTimeWorkRequest.Builder(PickItemSyncWorker::class.java)
                    .addTag("pick-item-sync-${barcode}")
                    .setInputData(data.build())
                    .setConstraints(builder.build())
                    .build()

                WorkManager.getInstance(context).enqueue(syncWorkRequest)

                fetchData()

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to mark as picked")
                Log.e(TAG, "Unable to mark as picked", e)
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
            fetchData(searchFor)
        }
    }

    fun fetchData(searchText: String? = null) {
        viewModelScope.launch {

            _loadingState.value = LoadingState.LOADING

            try {
                val extras = (context as Activity).intent.extras

                if (extras == null) {
                    _loadingState.value = LoadingState.error("Unable to get the picklist number")
                    return@launch
                }

                val pickListNumber = extras.getString("pick_list_name")!!
                pickListNo.value = pickListNumber


                val pickListDetailsItem = if (!searchText.isNullOrEmpty()) pickListService.getPickListItems(
                    pickListNumber, searchText
                )
                else pickListService.getPickListItems(pickListNumber)

                val pickListItem = ArrayList<PickListDetailsItem>()
                for (item in pickListDetailsItem) {
                    pickListItem.add(PickListDetailsItem(item.trackingId,
                        item.barcode, item.locationCode, item.wareHouseCode, item.picked))
                }
                _data.postValue(pickListItem)
            }catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to load pick list details")
                Log.e("PICKLIST", "Unable to get the picklist details from database", e)
                return@launch
            }

            _loadingState.value = LoadingState.LOADED
        }
    }

}