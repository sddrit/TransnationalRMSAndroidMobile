package com.tlrm.mobile.whapp.mvvm.pallateditems.viewmodel

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.LocationApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityPallatedItemsBinding
import com.tlrm.mobile.whapp.mvvm.pallateditems.model.PallateItem
import com.tlrm.mobile.whapp.mvvm.pallateditems.view.PallatedItemsViewModel
import com.tlrm.mobile.whapp.services.LocationService
import com.tlrm.mobile.whapp.util.LoadingState

class PallatedItemsActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var viewModel: PallatedItemsViewModel
    lateinit var binding: ActivityPallatedItemsBinding;
    lateinit var loaderIndicator: LinearProgressIndicator;

    var adapter: PallatedItemsAdapter? = null
    var items: ArrayList<PallateItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pallated_items)

        setupUI()
        setupViewModel()
        setupObserver()

        listView = this.findViewById(R.id.activity_pallated_items_list_view)
        val emptyView = findViewById<RelativeLayout>(android.R.id.empty)

        listView.visibility = View.INVISIBLE
        emptyView.visibility = View.INVISIBLE

        adapter = PallatedItemsAdapter(this, items)
        listView.adapter = adapter
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {

            private val visibleThreshold = 5
            private var previousTotal = 0
            private var loading = true

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                // Do nothing
            }

            override fun onScroll(
                view: AbsListView?, firstVisibleItem: Int,
                visibleItemCount: Int, totalItemCount: Int
            ) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false
                        previousTotal = totalItemCount
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    loading = true
                    viewModel.fetchNextPage()
                }
            }
        })

        viewModel.data.observe(this, {
            items.clear()
            items.addAll(it)
            adapter!!.notifyDataSetChanged()
        })
    }

    private fun setupObserver() {

        viewModel.loadingState.observe(this, Observer {

            val spinner =
                this.findViewById<LinearProgressIndicator>(R.id.activity_pallated_items_progress_indicator);
            val listView = this.findViewById<ListView>(R.id.activity_pallated_items_list_view)
            val emptyView = this.findViewById<RelativeLayout>(android.R.id.empty)

            when (it.status) {
                LoadingState.Status.SUCCESS -> {
                    spinner.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                    listView.emptyView = emptyView
                }
                LoadingState.Status.RUNNING -> {
                    spinner.visibility = View.VISIBLE
                }
                LoadingState.Status.FAILED -> {
                    spinner.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                    listView.emptyView = emptyView
                    val toast = Toast.makeText(
                        this@PallatedItemsActivity, it.msg,
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_pallated_items_progress_indicator)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        viewModel = PallatedItemsViewModel(
            this,
            LocationService(
                ServiceGenerator.createService(LocationApiService::class.java),
                database.paletteDao()
            )
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_pallated_items)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }


}

class PallatedItemsAdapter(
    private val context: Context,
    private val pallateItemList: ArrayList<PallateItem>
) : BaseAdapter() {

    private lateinit var cartonNo: TextView
    private lateinit var location: TextView
    private lateinit var date: TextView

    override fun getCount(): Int {
        return pallateItemList.count()
    }

    override fun getItem(position: Int): Any {
        return pallateItemList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        convertView =
            LayoutInflater.from(context).inflate(R.layout.pallated_list_item, parent, false)

        cartonNo = convertView.findViewById(R.id.pallated_list_item_carton_no)
        location = convertView.findViewById(R.id.pallated_list_item_location)
        date = convertView.findViewById(R.id.pallated_list_item_date)

        cartonNo.text = pallateItemList[position].barcode
        location.text = pallateItemList[position].locationCode
        date.text = pallateItemList[position].scannedDateTime

        return convertView
    }

}