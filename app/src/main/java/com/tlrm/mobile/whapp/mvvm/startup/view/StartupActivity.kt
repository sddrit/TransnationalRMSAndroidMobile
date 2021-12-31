package com.tlrm.mobile.whapp.mvvm.startup.view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.api.UserApiService
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.mvvm.login.view.LoginActivity
import com.tlrm.mobile.whapp.mvvm.startup.viewmodel.StartupViewModel
import com.tlrm.mobile.whapp.services.UserService
import com.tlrm.mobile.whapp.util.LoadingState


class StartupActivity : AppCompatActivity() {

    private lateinit var viewModel: StartupViewModel
    lateinit var loadMessageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        setupUI()
        setupViewModel()
        setupObserver()
    }

    private fun startMainActivity() {
        val mainIntent = Intent(this@StartupActivity, LoginActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun setupObserver() {
        viewModel.loadingState.observe(this, Observer {
            when(it.status) {
                LoadingState.Status.SUCCESS -> {
                    startMainActivity()
                }
                LoadingState.Status.RUNNING -> {
                    loadMessageTextView.text = it.msg
                }
                LoadingState.Status.FAILED -> {
                    loadMessageTextView.text = it.msg
                    startMainActivity()
                }
            }
        })
    }

    private fun setupUI() {
        loadMessageTextView = findViewById(R.id.activity_startup_load_message_text_view)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        viewModel = StartupViewModel(UserService(ServiceGenerator.createService(UserApiService::class.java),
            database.userDao()))
    }

}