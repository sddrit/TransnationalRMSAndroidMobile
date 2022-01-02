package com.tlrm.mobile.whapp.mvvm.picklistdetails.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityPickListDetailsBinding
import com.tlrm.mobile.whapp.mvvm.picklistdetails.model.PickListDetailsItem
import com.tlrm.mobile.whapp.mvvm.picklistdetails.viewmodel.PickListDetailsViewModel
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState

class PickListDetailsActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var pickListDetailsViewModel: PickListDetailsViewModel
    lateinit var binding: ActivityPickListDetailsBinding;
    lateinit var loaderIndicator: LinearProgressIndicator;

    var adapter: PickListDetailsAdapter? = null
    var pickList: ArrayList<PickListDetailsItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_list_details)

        setupUI()
        setupViewModel()
        setupObserver()

        listView = this.findViewById(R.id.picklist_list_details_list_view)
        adapter = PickListDetailsAdapter(this, pickList)
        listView.adapter = adapter

        pickListDetailsViewModel.data.observe(this, {
            pickList.clear()
            pickList.addAll(it)
            adapter!!.notifyDataSetChanged()
        })
    }

    override fun onRestart() {
        super.onRestart()
        pickListDetailsViewModel.fetchData()
    }

    private fun setupObserver() {
        pickListDetailsViewModel.loadingState.observe(this, Observer {
            when(it.status) {
                LoadingState.Status.SUCCESS -> {
                    loaderIndicator.apply {
                        visibility = View.GONE
                    }
                }
                LoadingState.Status.RUNNING -> {
                    loaderIndicator.apply {
                        visibility = View.VISIBLE
                    }
                }
                LoadingState.Status.FAILED -> {
                    loaderIndicator.apply {
                        visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_picklist_details_progress_indicator)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        pickListDetailsViewModel = PickListDetailsViewModel(this,
            PickListService(
                ServiceGenerator.createService(PickListApiService::class.java),
                DeviceService(
                    ServiceGenerator.createService(DeviceApiService::class.java),
                    SessionService(this.applicationContext)
                ),
                database.pickListDao()
            )
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_pick_list_details)
        binding.lifecycleOwner = this
        binding.viewModel = pickListDetailsViewModel
    }
}

class PickListDetailsAdapter(private val context: Context,
                      private val pickList: ArrayList<PickListDetailsItem>)
    : BaseAdapter()
{
    private lateinit var cartonNumber: TextView
    private lateinit var wareHouseCode: TextView
    private lateinit var location: TextView
    private lateinit var pickedImage: ImageView
    private lateinit var notPickedImage: ImageView

    override fun getCount(): Int {
        return pickList.count()
    }

    override fun getItem(position: Int): Any {
        return pickList[position]
    }

    override fun getItemId(position: Int): Long {
        return pickList[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.pick_list_details_item, parent,
            false)

        cartonNumber = convertView.findViewById(R.id.pick_list_details_item_pick_list_carton_no)
        wareHouseCode = convertView.findViewById(R.id.pick_list_details_item_pick_list_warehouse)
        location = convertView.findViewById(R.id.pick_list_details_item_pick_list_location)
        pickedImage = convertView.findViewById(R.id.pick_list_details_item_pick_list_picked)
        notPickedImage = convertView.findViewById(R.id.pick_list_details_item_pick_list_not_picked)

        cartonNumber.text = pickList[position].barcode
        wareHouseCode.text = pickList[position].warehouseCode
        location.text = pickList[position].locationCode

        if (pickList[position].picked) {
            pickedImage.visibility = View.VISIBLE
            notPickedImage.visibility = View.GONE
        } else {
            pickedImage.visibility = View.GONE
            notPickedImage.visibility = View.VISIBLE
        }

        return convertView
    }

}