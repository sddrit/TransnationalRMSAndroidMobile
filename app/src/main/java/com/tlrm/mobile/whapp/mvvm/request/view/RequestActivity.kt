package com.tlrm.mobile.whapp.mvvm.request.view

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
import com.tlrm.mobile.whapp.api.RequestApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityRequestBinding
import com.tlrm.mobile.whapp.mvvm.request.model.RequestListItem
import com.tlrm.mobile.whapp.mvvm.request.viewmodel.RequestViewModel
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.util.LoadingState
import android.widget.AbsListView

class RequestActivity : AppCompatActivity() {

    var isLoadMore = false;

    lateinit var listView: ListView
    lateinit var viewModel: RequestViewModel
    lateinit var binding: ActivityRequestBinding;
    lateinit var loaderIndicator: LinearProgressIndicator;

    var adapter: RequestAdapter? = null
    var items: ArrayList<RequestListItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        setupUI()
        setupViewModel()
        setupObserver()

        listView = this.findViewById(R.id.activity_request_list_list_view)
        val emptyView = findViewById<RelativeLayout>(android.R.id.empty)

        listView.visibility = View.INVISIBLE
        emptyView.visibility = View.INVISIBLE

        adapter = RequestAdapter(this, items)
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
                this.findViewById<LinearProgressIndicator>(R.id.activity_request_progress_indicator);
            val listView = this.findViewById<ListView>(R.id.activity_request_list_list_view)
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
                        this@RequestActivity, it.msg,
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_request_progress_indicator)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        viewModel = RequestViewModel(
            this,
                RequestService(
                ServiceGenerator.createService(RequestApiService::class.java),
            )
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_request)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }
}



class RequestAdapter(
    private val context: Context,
    private val requestListItem: ArrayList<RequestListItem>
) : BaseAdapter() {

    private lateinit var requestNo: TextView
    private lateinit var name: TextView
    private lateinit var date: TextView

    override fun getCount(): Int {
        return requestListItem.count()
    }

    override fun getItem(position: Int): Any {
        return requestListItem[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        convertView =
            LayoutInflater.from(context).inflate(R.layout.request_list_item, parent, false)

        requestNo = convertView.findViewById(R.id.request_list_item_request_no)
        name = convertView.findViewById(R.id.request_list_item_name)
        date = convertView.findViewById(R.id.request_list_item_date)

        requestNo.text = requestListItem[position].requestNo
        name.text = requestListItem[position].name
        date.text = requestListItem[position].deliveryDate

        return convertView
    }

}

