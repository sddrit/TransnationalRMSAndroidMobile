package com.tlrm.mobile.whapp.mvvm.main.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.databinding.ActivityMainBinding
import com.tlrm.mobile.whapp.mvvm.main.viewmodel.MainViewModel
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.workers.RemovePickListSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViewModel()

        configureRemovePickListSyncWorker()
    }

    private fun setupViewModel() {
        val sessionService = SessionService(this.applicationContext)
        viewModel = MainViewModel(
            this,
            DeviceService(
                ServiceGenerator.createService(DeviceApiService::class.java),
                sessionService
            ),
            sessionService
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun configureRemovePickListSyncWorker() {
        val constraints = Constraints.Builder()
            .build()
        val work = PeriodicWorkRequestBuilder<RemovePickListSyncWorker>(5, TimeUnit.MINUTES)
            .addTag("remove-picklist-sync-worker")
            .setConstraints(constraints)
            .build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(work)
    }


}