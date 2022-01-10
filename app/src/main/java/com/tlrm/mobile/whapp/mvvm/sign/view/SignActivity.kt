package com.tlrm.mobile.whapp.mvvm.sign.view

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.RequestApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivitySignBinding
import com.tlrm.mobile.whapp.mvvm.sign.viewmodel.SignViewModel
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState

class SignActivity : AppCompatActivity() {

    private val TAG = SignActivity::class.java.simpleName

    private lateinit var binding: ActivitySignBinding

    private lateinit var viewModel: SignViewModel;
    lateinit var loaderIndicator: LinearProgressIndicator;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        setupUI()
        setupViewModel()
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.loadingState.observe(this, Observer {

            val spinner =
                this.findViewById<LinearProgressIndicator>(R.id.activity_sign_progress_indicator);

            when (it.status) {
                LoadingState.Status.SUCCESS -> {
                    spinner.visibility = View.GONE
                }
                LoadingState.Status.RUNNING -> {
                    spinner.visibility = View.VISIBLE
                }
                LoadingState.Status.FAILED -> {
                    spinner.visibility = View.GONE
                    val toast = Toast.makeText(
                        this@SignActivity, it.msg,
                        Toast.LENGTH_LONG
                    )
                    toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
        loaderIndicator = findViewById(R.id.activity_sign_progress_indicator)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        viewModel = SignViewModel(
            this,
            SessionService(this),
            RequestService(
                database.requestDao(),
                ServiceGenerator.createService(RequestApiService::class.java)
            )
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_sign)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }
}