package com.tlrm.mobile.whapp.mvvm.requestdetails.view

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
import com.daimajia.swipe.SwipeLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.RequestApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityRequestDetailsBinding
import com.tlrm.mobile.whapp.mvvm.requestdetails.model.RequestDetailsItem
import com.tlrm.mobile.whapp.mvvm.requestdetails.viewmodel.RequestDetailsViewModel
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState

interface RequestDetailsEvent {
    fun scannedItem(cartonNumber: String)
    fun scannedItem(fromCartonNumber: String, toCartonNumber: String)
}

class RequestDetailsActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var viewModel: RequestDetailsViewModel
    lateinit var binding: ActivityRequestDetailsBinding;
    lateinit var loaderIndicator: LinearProgressIndicator;

    var adapter: RequestDetailsAdapter? = null
    var items: ArrayList<RequestDetailsItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_details)

        setupUI()
        setupViewModel()
        setupObserver()

        listView = this.findViewById(R.id.activity_request_details_list_view)
        val emptyView = findViewById<RelativeLayout>(android.R.id.empty)

        listView.visibility = View.INVISIBLE
        emptyView.visibility = View.INVISIBLE

        adapter = RequestDetailsAdapter(this, items)

        adapter!!.setRequestDetailsEvent(object: RequestDetailsEvent {
            override fun scannedItem(cartonNumber: String) {
                viewModel.scan(cartonNumber)
            }

            override fun scannedItem(fromCartonNumber: String, toCartonNumber: String) {
                viewModel.scan(fromCartonNumber, toCartonNumber)
            }
        })

        listView.adapter = adapter

        viewModel.data.observe(this, {
            items.clear()
            items.addAll(it)
            adapter!!.notifyDataSetChanged()
        })
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.init()
    }

    private fun setupObserver() {

        viewModel.loadingState.observe(this, Observer {

            val spinner =
                this.findViewById<LinearProgressIndicator>(R.id.activity_request_details_progress_indicator);
            val listView = this.findViewById<ListView>(R.id.activity_request_details_list_view)
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
                        this@RequestDetailsActivity, it.msg,
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_request_details_progress_indicator)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        viewModel = RequestDetailsViewModel(
            this,
            SessionService(this),
            RequestService(
                database.requestDao(),
                ServiceGenerator.createService(RequestApiService::class.java)
            )
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_request_details)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }
}

class RequestDetailsAdapter(
    private val context: Context,
    private val requestDetailItems: ArrayList<RequestDetailsItem>
) : BaseAdapter() {

    private lateinit var cartonNumber: TextView
    private lateinit var scannedImage: ImageView
    private lateinit var notScannedImage: ImageView

    private var event: RequestDetailsEvent? = null

    fun setRequestDetailsEvent(event: RequestDetailsEvent) {
        this.event = event
    }

    override fun getCount(): Int {
        return requestDetailItems.count()
    }

    override fun getItem(position: Int): Any {
        return requestDetailItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        convertView =
            LayoutInflater.from(context).inflate(R.layout.request_details_item, parent, false)

        cartonNumber = convertView.findViewById(R.id.request_details_item_carton_no)
        scannedImage = convertView.findViewById(R.id.request_details_item_scanned)
        notScannedImage = convertView.findViewById(R.id.request_details_item_not_scanned)

        val requestListItem = requestDetailItems[position]

        if (requestListItem.isEmptyRange) {
            cartonNumber.text = "${requestListItem.fromCartonNumber} - ${requestListItem.toCartonNumber}"
        }else {
            cartonNumber.text = requestListItem.cartonNumber
        }

        if(requestListItem.scanned) {
            scannedImage.visibility = View.VISIBLE
            notScannedImage.visibility = View.GONE
        }else {
            scannedImage.visibility = View.GONE
            notScannedImage.visibility = View.VISIBLE
        }

        val swipeLayout = convertView as SwipeLayout
        swipeLayout.showMode = SwipeLayout.ShowMode.LayDown;

        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, convertView.findViewById(R.id.request_details_item_bottom_wrapper));

        val scannedButton = convertView.findViewById<Button>(R.id.request_details_item_bottom_picked_button);

        scannedButton.isEnabled = !requestListItem.scanned

        scannedButton.setOnClickListener {
            if (requestListItem.isEmptyRange) {
                event?.scannedItem(requestListItem.fromCartonNumber!!, requestListItem.toCartonNumber!!)
            } else {
                event?.scannedItem(requestListItem.cartonNumber!!)
            }
        }

        swipeLayout.addSwipeListener(object : SwipeLayout.SwipeListener {
            override fun onClose(layout: SwipeLayout) {
                //when the SurfaceView totally cover the BottomView.
            }

            override fun onUpdate(layout: SwipeLayout, leftOffset: Int, topOffset: Int) {
                //you are swiping.
            }

            override fun onStartOpen(layout: SwipeLayout) {}
            override fun onOpen(layout: SwipeLayout) {
                //when the BottomView totally show.
            }

            override fun onStartClose(layout: SwipeLayout) {}
            override fun onHandRelease(layout: SwipeLayout, xvel: Float, yvel: Float) {
                //when user's hand released.
            }
        })

        return convertView
    }

}