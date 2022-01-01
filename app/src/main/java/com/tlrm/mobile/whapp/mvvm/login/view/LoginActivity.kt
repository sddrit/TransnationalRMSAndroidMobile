package com.tlrm.mobile.whapp.mvvm.login.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tlrm.mobile.whapp.mvvm.main.view.MainActivity
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.api.UserApiService
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityLoginBinding
import com.tlrm.mobile.whapp.mvvm.login.viewmodel.LoginViewModel
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.services.UserService
import com.tlrm.mobile.whapp.util.LoadingState

class LoginActivity : AppCompatActivity() {

    private lateinit var loaderIndicator: LinearProgressIndicator

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        setupViewModel()
        setupUI()
        setupObserver()
    }

    private fun setupObserver() {

        val spinner = this.
                findViewById<LinearProgressIndicator>(R.id.activity_login_progress_indicator);

        loginViewModel.loadingState.observe(this, Observer {
            when(it.status) {
                LoadingState.Status.SUCCESS -> {
                    spinner.visibility = View.INVISIBLE
                    startHomeActivity()
                }
                LoadingState.Status.RUNNING -> {
                    spinner.visibility = View.VISIBLE
                }
                LoadingState.Status.FAILED -> {
                    spinner.visibility = View.INVISIBLE
                    val toast = Toast.makeText(this@LoginActivity, it.msg,
                        Toast.LENGTH_LONG)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_login_progress_indicator)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        loginViewModel = LoginViewModel(this,
            SessionService(this),
            UserService(
                ServiceGenerator.createService(UserApiService::class.java),
                database.userDao())
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this
        binding.loginViewModel = loginViewModel
    }

    private fun startHomeActivity() {
        val homeIntent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(homeIntent)
        finish()
    }

}