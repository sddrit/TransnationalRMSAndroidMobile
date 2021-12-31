package com.tlrm.mobile.whapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.tlrm.mobile.whapp.util.dp
import android.view.Gravity
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PickListScanActivity : AppCompatActivity() {

    private val PERMISSION_CAMERA_REQUEST = 1
    private val TAG = PickListScanActivity::class.java.simpleName

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var beepManager: BeepManager
    private lateinit var pickListName: TextView
    private lateinit var pickListCount: TextView
    private lateinit var pickListPicked: TextView
    private var lastText: String? = null

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || lastText == result.text) {
                return
            }
            lastText = result.text
            beepManager.playBeepSoundAndVibrate()
            val toast = Toast.makeText(this@PickListScanActivity, "Scanned Item $lastText",
                Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
        }
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_list_scan)

        pickListName = findViewById<TextView>(R.id.pick_list_scan_pick_list_name)
        pickListCount = findViewById<TextView>(R.id.pick_list_scan_item_count)
        pickListPicked = findViewById<TextView>(R.id.pick_list_scan_item_picked)

        val extras = intent.extras

        if(extras != null) {
            pickListName.text = extras.getString("pick_list_name")
            pickListCount.text = extras.getInt("pick_list_count").toString()
            pickListPicked.text = extras.getInt("pick_list_picked").toString()
        }

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

    fun complete() {

    }

    fun triggerScan(view: View?) {
        barcodeView.decodeSingle(callback)
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