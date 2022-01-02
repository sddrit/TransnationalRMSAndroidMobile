package com.tlrm.mobile.whapp.mvvm.main.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.databinding.ActivityMainBinding
import com.tlrm.mobile.whapp.mvvm.main.viewmodel.MainViewModel
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.SessionService

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViewModel()
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


}