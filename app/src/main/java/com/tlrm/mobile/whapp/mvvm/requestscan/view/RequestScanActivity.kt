package com.tlrm.mobile.whapp.mvvm.requestscan.view

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.RingtoneManager
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
import com.tlrm.mobile.whapp.api.RequestApiService
import com.tlrm.mobile.whapp.api.ServiceGenerator
import com.tlrm.mobile.whapp.database.AppDatabase
import com.tlrm.mobile.whapp.databinding.ActivityRequestScanBinding
import com.tlrm.mobile.whapp.mvvm.requestscan.viewmodel.RequestScanViewModel
import com.tlrm.mobile.whapp.services.RequestService
import com.tlrm.mobile.whapp.services.SessionService
import com.tlrm.mobile.whapp.util.LoadingState
import com.tlrm.mobile.whapp.util.dp

class RequestScanActivity : AppCompatActivity() {
    private val PERMISSION_CAMERA_REQUEST = 1
    private val TAG = RequestScanActivity::class.java.simpleName

    private lateinit var viewModel: RequestScanViewModel;
    private lateinit var binding: ActivityRequestScanBinding;

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
            viewModel.scan(result.text)

        }
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_scan)

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
        viewModel.loadingState.observe(this, Observer {
            when(it.status) {
                LoadingState.Status.SUCCESS -> {

                }
                LoadingState.Status.RUNNING -> {

                }
                LoadingState.Status.FAILED -> {
                    this.alert(it.msg!!)
                }
            }
        })
    }

    private fun setupUI() {
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this.applicationContext)
        viewModel = RequestScanViewModel(this,
            SessionService(this),
            RequestService(
                database.requestDao(),
                ServiceGenerator.createService(RequestApiService::class.java)
            )
        )
        binding = DataBindingUtil
            .setContentView(this, R.layout.activity_request_scan)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun alert(message: String) {

        barcodeView.pause()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationSound = RingtoneManager.getRingtone(applicationContext, defaultSoundUri)
        notificationSound.play()

        val builder = AlertDialog.Builder(this)

        with(builder)
        {
            setMessage(message)
            setPositiveButton("OK", DialogInterface.OnClickListener(function = positiveButtonClick))
            show()
        }
    }

    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        barcodeView.resume();
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