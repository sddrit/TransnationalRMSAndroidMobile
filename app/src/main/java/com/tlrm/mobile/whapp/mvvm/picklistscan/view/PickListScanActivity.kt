package com.tlrm.mobile.whapp.mvvm.picklistscan.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.tlrm.mobile.whapp.R
import com.tlrm.mobile.whapp.api.DeviceApiService
import com.tlrm.mobile.whapp.api.PickListApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityPickListScanBinding
import com.tlrm.mobile.whapp.mvvm.picklistscan.viewmodel.PickListScanViewModel
import com.tlrm.mobile.whapp.services.DeviceService
import com.tlrm.mobile.whapp.services.PickListService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import com.tlrm.mobile.whapp.util.dp
import kotlinx.coroutines.runBlocking

class PickListScanActivity : AppCompatActivity() {

    private val PERMISSION_CAMERA_REQUEST = 1
    private val TAG = PickListScanActivity::class.java.simpleName

    private lateinit var pickListScanViewModel: PickListScanViewModel;
    private lateinit var binding: ActivityPickListScanBinding;

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var beepManager: BeepManager
    private var lastText: String? = null

    private val callback: BarcodeCallback = object : BarcodeCallback {
        @Synchronized
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || lastText == result.text) {
                return
            }
            lastText = result.text
            beepManager.playBeepSoundAndVibrate()
            pickListScanViewModel.scan(result.text)

        }
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_list_scan)

        setupUI()
        setupViewModel()
        setupObserver()

        if (isCameraPermissionGranted()) {
           startScanner()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                startScanner()
            } else {
                Log.e(TAG, "no camera permission for scan items")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        barcodeView.pause()
        super.onBackPressed()
    }

    fun triggerScan(view: View?) {
        barcodeView.decodeSingle(callback)
    }

    private fun setupObserver() {
        pickListScanViewModel.loadingState.observe(this, Observer {
            when(it.status) {
                LoadingState.Status.SUCCESS -> {

                }
                LoadingState.Status.RUNNING -> {

                }
                LoadingState.Status.FAILED -> {
                    val toast = Toast.makeText(this@PickListScanActivity, it.msg,
                        Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                    toast.show()
                }
            }
        })
    }

    private fun setupUI() {
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        pickListScanViewModel = PickListScanViewModel(this,
            SessionService(this),
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
            .setContentView(this, R.layout.activity_pick_list_scan)
        binding.lifecycleOwner = this
        binding.viewModel = pickListScanViewModel
    }

    private fun startScanner() {
        barcodeView = findViewById(R.id.barcode_scanner)
        val formats: Collection<BarcodeFormat> =
            listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39, BarcodeFormat.CODE_128)
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.initializeFromIntent(intent)
        barcodeView.decodeContinuous(callback)
        barcodeView.setStatusText("Place a barcode inside the rectangle to scan it")
        barcodeView.statusView.setPadding(10.dp, 0, 10.dp, 10.dp)
        beepManager = BeepManager(this)
        barcodeView.resume()
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}