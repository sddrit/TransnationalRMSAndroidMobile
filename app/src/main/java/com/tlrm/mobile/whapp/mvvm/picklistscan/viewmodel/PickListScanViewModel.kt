package com.tlrm.mobile.whapp.mvvm.picklistscan.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import com.tlrm.mobile.whapp.mvvm.picklistdetails.model.PickListDetailsItem
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import androidx.work.NetworkType
import androidx.work.WorkManager

import androidx.work.OneTimeWorkRequest
import com.tlrm.mobile.whapp.workers.PickItemSyncWorker
import java.time.ZoneOffset


class PickListScanViewModel(
    private val context: Context,
    private val sessionService: SessionService,
    private val pickListService: PickListService
) : ViewModel() {

    private val TAG = PickListScanViewModel::class.java.simpleName

    val pickListNo: MutableLiveData<String> = MutableLiveData<String>();
    val count: MutableLiveData<String> = MutableLiveData<String>();
    val picked: MutableLiveData<String> = MutableLiveData<String>();

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<PickListDetailsItem>>(emptyList())

    val data: LiveData<List<PickListDetailsItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    init {
        fetchData()
    }

    fun scan(barcode: String) {
        viewModelScope.launch {
            var pickListItem = getPickListItemByBarcode(barcode)

            if (pickListItem == null) {
                _loadingState.value = LoadingState.error("$barcode is not exists in this pick list")
                return@launch
            }

            val user = sessionService.getUser()
            val currentDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            try {

                if (pickListItem.picked) {
                   return@launch
                }

                pickListService.markAsPicked(pickListItem.barcode, user.id, currentDateTime)

                val builder: Constraints.Builder = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)

                val data: Data.Builder = Data.Builder()
                data.putString("carton_no", pickListItem.barcode)

                val syncWorkRequest = OneTimeWorkRequest.Builder(PickItemSyncWorker::class.java)
                    .addTag("pick-item-sync-${pickListItem.barcode}")
                    .setInputData(data.build())
                    .setConstraints(builder.build())
                    .build()

                WorkManager.getInstance(context).enqueue(syncWorkRequest)

                val newPickedCount = picked.value!!.toInt() + 1
                picked.value = newPickedCount.toString()

            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to mark as picked")
                Log.e(TAG, "Unable to mark as picked", e)
            }
        }
    }

    fun complete() {
        (context as AppCompatActivity).onBackPressed()
    }

    private fun getPickListItemByBarcode(barcode: String): PickListDetailsItem? {
        return data.value!!.firstOrNull { it.barcode == barcode };
    }

    private fun fetchData() {
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

                val pickListDetails = pickListService.getPickListDetails(pickListNumber)

                pickListNo.value = pickListDetails.pickListNo
                count.value = pickListDetails.count.toString()
                picked.value = pickListDetails.picked.toString()

                val pickListDetailsItem = pickListService.getPickListItems(pickListNumber)

                val pickListItem = ArrayList<PickListDetailsItem>()
                for (item in pickListDetailsItem) {
                    pickListItem.add(
                        PickListDetailsItem(
                            item.trackingId,
                            item.barcode, item.locationCode, item.wareHouseCode, item.picked
                        )
                    )
                }

                _data.postValue(pickListItem)
            } catch (e: Exception) {
                _loadingState.value = LoadingState.error("Unable to load pick list details")
                Log.e(TAG, "Unable to get the picklist details from database", e)
                return@launch
            }

            _loadingState.value = LoadingState.LOADED
        }
    }

}