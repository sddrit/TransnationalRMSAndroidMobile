package com.tlrm.mobile.whapp.mvvm.pallatedetails.view

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.LocationApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityPallateDetailsBinding
import com.tlrm.mobile.whapp.mvvm.pallatedetails.model.PallateDetailsSummeryListItem
import com.tlrm.mobile.whapp.mvvm.pallatedetails.viewmodel.PallateDetailsViewModel
import com.tlrm.mobile.whapp.services.LocationService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState

class PallateDetailsActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var viewModel: PallateDetailsViewModel
    lateinit var binding: ActivityPallateDetailsBinding;
    lateinit var loaderIndicator: LinearProgressIndicator;

    var adapter: PallateDetailsAdapter? = null
    var pallateDetailsSummeryItems: ArrayList<PallateDetailsSummeryListItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pallate_details)

        setupUI()
        setupViewModel()
        setupObserver()

        listView = this.findViewById(R.id.activity_pallate_details_list_view)
        listView.emptyView = findViewById(android.R.id.empty)

        adapter = PallateDetailsAdapter(this, pallateDetailsSummeryItems)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val element = adapter!!.getItem(position) as PallateDetailsSummeryListItem
            viewModel.gotoDetails(element)
        }

        viewModel.data.observe(this, {
            pallateDetailsSummeryItems.clear()
            pallateDetailsSummeryItems.addAll(it)
            adapter!!.notifyDataSetChanged()
        })
    }

    private fun setupObserver() {

        val spinner =
            this.findViewById<LinearProgressIndicator>(R.id.activity_pallate_details_loader);

        viewModel.loadingState.observe(this, Observer {
            when (it.status) {
                LoadingState.Status.SUCCESS -> {
                    spinner.visibility = View.GONE
                }
                LoadingState.Status.RUNNING -> {
                    spinner.visibility = View.VISIBLE
                }
                LoadingState.Status.FAILED -> {
                    spinner.visibility = View.GONE
                    val toast = Toast.makeText(
                        this@PallateDetailsActivity, it.msg,
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_pallate_details_loader)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        viewModel = PallateDetailsViewModel(
            this,
            SessionService(this),
            LocationService(
                ServiceGenerator.createService(LocationApiService::class.java),
                database.paletteDao()
            )
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_pallate_details)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

}

class PallateDetailsAdapter(
    private val context: Context,
    private val pallateDetailsSummeryItems: ArrayList<PallateDetailsSummeryListItem>
) : BaseAdapter() {

    private lateinit var date: TextView
    private lateinit var count: TextView

    override fun getCount(): Int {
        return pallateDetailsSummeryItems.count()
    }

    override fun getItem(position: Int): Any {
        return pallateDetailsSummeryItems[position]
    }

    override fun getItemId(position: Int): Long {
        return pallateDetailsSummeryItems[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        convertView =
            LayoutInflater.from(context).inflate(R.layout.pallate_details_list_item, parent, false)
        date = convertView.findViewById(R.id.pallate_details_list_item_date)
        count = convertView.findViewById(R.id.pallate_details_list_item_count)
        date.text = pallateDetailsSummeryItems[position].scanDate
        count.text = pallateDetailsSummeryItems[position].cartonCount.toString()
        return convertView
    }

}



