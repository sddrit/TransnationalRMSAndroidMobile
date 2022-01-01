package com.tlrm.mobile.whapp.mvvm.picklist.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tlrm.mobile.whapp.mvvm.picklist.model.PickListItem
import com.tlrm.mobile.whapp.mvvm.picklistdetails.view.PickListDetailsActivity
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.util.LoadingState
import kotlinx.coroutines.launch

class PickListViewModel(private val context: Context,
                        private val pickListService: PickListService): ViewModel() {

    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<PickListItem>>(emptyList())

    val data: LiveData<List<PickListItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    init {
        fetchData()
    }

    fun gotoToPickListDetails(pickListItem: PickListItem) {
        val intent = Intent(context, PickListDetailsActivity::class.java)
        intent.putExtra("pick_list_id", pickListItem.id)
        intent.putExtra("pick_list_name", pickListItem.pickListName)
        intent.putExtra("pick_list_count", pickListItem.count)
        intent.putExtra("pick_list_picked", pickListItem.picked)
        context.startActivity(intent)
    }

     fun fetchData() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.loading("Syncing picklist from server")

            try {
                pickListService.refresh()
            } catch (e: Exception) {
                Log.e("PICKLIST", "Unable to refresh picklist", e)
            }

            try{
                val pickListDetailsItem = pickListService.getPickListDetailItems()
                val pickListItem = ArrayList<PickListItem>()
                for (item in pickListDetailsItem) {
                    pickListItem.add(PickListItem(0, item.pickListNo, item.count, item.picked))
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