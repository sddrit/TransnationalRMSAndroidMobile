package com.tlrm.mobile.whapp.mvvm.picklist.view

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
import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityPickListBinding
import com.tlrm.mobile.whapp.mvvm.picklist.model.PickListItem
import com.tlrm.mobile.whapp.mvvm.picklist.viewmodel.PickListViewModel
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState

interface PickListItemEvent {
    fun deleteItem(number: String)
}

class PickListActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var pickListViewModel: PickListViewModel
    lateinit var binding: ActivityPickListBinding;
    lateinit var loaderIndicator: LinearProgressIndicator;

    var adapter: PickListAdapter? = null
    var pickList: ArrayList<PickListItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_list)

        setupUI()
        setupViewModel()
        setupObserver()

        listView = this.findViewById(R.id.picklist_list_list_view)
        listView.visibility = View.INVISIBLE
        adapter = PickListAdapter(this, pickList)

        adapter!!.setPickListEvent(object : PickListItemEvent {
            override fun deleteItem(number: String) {
                pickListViewModel.deletePickList(number)
            }
        })

        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val element = adapter!!.getItem(position) as PickListItem
            pickListViewModel.gotoToPickListDetails(element)
        }

        pickListViewModel.data.observe(this, {
            pickList.clear()
            pickList.addAll(it)
            adapter!!.notifyDataSetChanged()
        })

    }

    override fun onRestart() {
        super.onRestart()
        pickListViewModel.fetchData()
    }

    private fun setupObserver() {

        val spinner = this.
            findViewById<LinearProgressIndicator>(R.id.activity_picklist_progress_indicator);
        val listItemsView = this.findViewById<ListView>(R.id.picklist_list_list_view)

        pickListViewModel.loadingState.observe(this, Observer {
            when(it.status) {
                LoadingState.Status.SUCCESS -> {
                    spinner.visibility = View.GONE
                    listItemsView.visibility = View.VISIBLE
                }
                LoadingState.Status.RUNNING -> {
                    spinner.visibility = View.VISIBLE
                }
                LoadingState.Status.FAILED -> {
                    spinner.visibility = View.GONE
                    listItemsView.visibility = View.VISIBLE
                    val toast = Toast.makeText(
                        this, it.msg,
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_picklist_progress_indicator)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        pickListViewModel = PickListViewModel(this,
            SessionService(this.applicationContext),
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
            .setContentView(this, R.layout.activity_pick_list)
        binding.lifecycleOwner = this
        binding.pickListViewModel = pickListViewModel
    }
}

class PickListAdapter(private val context: Context,
                      private val pickList: ArrayList<PickListItem>)
    : BaseAdapter()
{
    private lateinit var pickListNumber: TextView
    private lateinit var count: TextView
    private lateinit var picked: TextView

    private var pickListEvent: PickListItemEvent? = null

    fun setPickListEvent(event: PickListItemEvent) {
        this.pickListEvent = event;
    }

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
        convertView = LayoutInflater.from(context).inflate(R.layout.pick_list_item, parent, false)
        pickListNumber = convertView.findViewById(R.id.pick_list_item_pick_list_number)
        count = convertView.findViewById(R.id.pick_list_item_count)
        picked = convertView.findViewById(R.id.pick_list_item_picked)
        pickListNumber.text = pickList[position].pickListName
        count.text = pickList[position].count.toString()
        picked.text = pickList[position].picked.toString()

        val swipeLayout = convertView as SwipeLayout
        swipeLayout.showMode = SwipeLayout.ShowMode.LayDown;

        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, convertView.findViewById(R.id.pick_list_item_bottom_wrapper));

        val deleteButton = convertView.findViewById<Button>(R.id.pick_list_item_bottom_delete_button);
        deleteButton.setOnClickListener {
            pickListEvent?.deleteItem(pickList[position].pickListName)
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
