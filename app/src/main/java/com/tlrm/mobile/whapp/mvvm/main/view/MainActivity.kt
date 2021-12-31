package com.tlrm.mobile.whapp.mvvm.main.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.databinding.ActivityMainBinding
import com.tlrm.mobile.whapp.mvvm.main.viewmodel.MainViewModel
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
        viewModel = MainViewModel(this, SessionService(this))
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }


}