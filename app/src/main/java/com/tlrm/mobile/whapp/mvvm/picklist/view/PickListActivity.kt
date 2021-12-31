package com.tlrm.mobile.whapp.mvvm.picklist.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.tlrm.mobile.whapp.PickListScanActivity
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityPickListBinding
import com.tlrm.mobile.whapp.mvvm.picklist.model.PickListItem
import com.tlrm.mobile.whapp.mvvm.picklist.viewmodel.PickListViewModel
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.util.LoadingState

class PickListActivity : AppCompatActivity() {

    lateinit var listView: ListView
    lateinit var pickListViewModel: PickListViewModel
    lateinit var binding: ActivityPickListBinding;
    lateinit var loaderIndicator: CircularProgressIndicator;

    var adapter: PickListAdapter? = null
    var pickList: ArrayList<PickListItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_list)

        setupUI()
        setupViewModel()
        setupObserver()

        listView = this.findViewById(R.id.picklist_list_list_view)
        adapter = PickListAdapter(this, pickList)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->
            val element = adapter!!.getItem(position) as PickListItem
            val intent = Intent(this, PickListScanActivity::class.java)
            intent.putExtra("pick_list_id", element.id)
            intent.putExtra("pick_list_name", element.pickListName)
            intent.putExtra("pick_list_count", element.count)
            intent.putExtra("pick_list_picked", element.picked)
            startActivity(intent)
        }

        pickListViewModel.data.observe(this, {
            pickList.clear()
            pickList.addAll(it)
            adapter!!.notifyDataSetChanged()
        })

    }

    private fun setupObserver() {
        pickListViewModel.loadingState.observe(this, Observer {
            when(it.status) {
                LoadingState.Status.SUCCESS -> {
                    loaderIndicator.visibility = View.INVISIBLE
                }
                LoadingState.Status.RUNNING -> {
                    loaderIndicator.visibility = View.VISIBLE
                }
                LoadingState.Status.FAILED -> {
                    loaderIndicator.visibility = View.INVISIBLE
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
            PickListService(
                ServiceGenerator.createService(PickListApiService::class.java),
                database.pickListDao())
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
        return convertView
    }

}
