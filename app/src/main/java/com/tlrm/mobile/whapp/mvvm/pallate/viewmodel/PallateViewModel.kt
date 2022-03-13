package com.tlrm.mobile.whapp.mvvm.pallate.viewmodel

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.tlrm.mobile.whapp.database.entities.PallateEntity
import com.tlrm.mobile.whapp.database.entities.UserEntity
import com.tlrm.mobile.whapp.mvvm.picklistdetails.model.PickListDetailsItem
import com.tlrm.mobile.whapp.services.LocationService
import com.tlrm.mobile.whapp.services.MetadataService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.services.StorageFieldDefinition
import com.tlrm.mobile.whapp.util.LoadingState
import com.tlrm.mobile.whapp.workers.PallateSyncWorker
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneOffset

class PallateViewModel(
    private val context: Context,
    private val metadataService: MetadataService,
    private val locationService: LocationService,
    private val sessionService: SessionService,
) : ViewModel() {

    private val TAG = PallateViewModel::class.java.simpleName

    private val storageFieldDefinition: StorageFieldDefinition;
    private val user: UserEntity;
    private val _loadingState = MutableLiveData<LoadingState>()
    private val _data = MutableLiveData<List<PickListDetailsItem>>(emptyList())

    private var toast: Toast? = null;

    val data: LiveData<List<PickListDetailsItem>>
        get() = _data

    val loadingState: LiveData<LoadingState>
        get() = _loadingState

    val location: MutableLiveData<String> = MutableLiveData<String>();
    val hasLocation: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    init {
        if (!hasLocation.value!!) {
            location.value = "Scan Location"
        }
        storageFieldDefinition = metadataService.getStorageFieldDefinition()!!
        user = sessionService.getUser()
    }

    fun complete() {
        (context as AppCompatActivity).onBackPressed()
    }

    fun scan(barcode: String) {

        viewModelScope.launch {
            //If scan a location, set the current location

            if (barcode.length == storageFieldDefinition.locationLength) {
                location.value = barcode
                hasLocation.value = true
                return@launch
            }

            if (!hasLocation.value!!) {
                _loadingState.value = LoadingState.error("You have to scan a " +
                        "location first for pallete")
                return@launch
            }

            // If scan a carton, set the current location

            if (barcode.length == storageFieldDefinition.cartonLength) {
                try {
                    val currentDateTime = OffsetDateTime.now(ZoneOffset.UTC)
                    val pallateId = locationService.pallate(
                        PallateEntity(
                            0, barcode, location.value!!, "1",
                            user.userName, currentDateTime, false, null
                        )
                    )

                    val builder: Constraints.Builder = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)

                    val data: Data.Builder = Data.Builder()
                    data.putInt("pallate_id", pallateId)

                    val syncWorkRequest = OneTimeWorkRequest.Builder(PallateSyncWorker::class.java)
                        .addTag("pallate-sync-${pallateId}")
                        .setInputData(data.build())
                        .setConstraints(builder.build())
                        .build()

                    WorkManager.getInstance(context).enqueue(syncWorkRequest)

                    if (toast != null) {
                        toast!!.cancel()
                    }

                    toast = Toast.makeText(
                        context, "Pallet carton number ${barcode} at location ${location.value}",
                        Toast.LENGTH_SHORT
                    )
                    toast!!.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 10)
                    toast!!.show()

                } catch (e: Exception) {
                    Log.e(TAG, "Unable to add pallate details", e)
                    _loadingState.value = LoadingState.error("Unable to add pallate details")
                }
            }
        }


    }

}