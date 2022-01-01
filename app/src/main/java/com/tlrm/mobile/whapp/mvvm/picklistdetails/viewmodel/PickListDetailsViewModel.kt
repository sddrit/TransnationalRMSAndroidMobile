package com.tlrm.mobile.whapp.mvvm.picklistdetails.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.mvvm.picklistscan.view.PickListScanActivity
import com.tlrm.mobile.whapp.mvvm.picklistdetails.model.PickListDetailsItem
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch

class PickListDetailsViewModel(
    private val context: Context,
    private val pickListService: PickListService
): ViewModel() {

    val pickListNo: MutableLiveData<String> = MutableLiveData<String>();

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<PickListDetailsItem>>(emptyList())

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

    fun fetchData() {
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

                val pickListDetailsItem = pickListService.getPickListItems(pickListNumber)

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