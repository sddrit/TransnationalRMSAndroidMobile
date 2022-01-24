package com.tlrm.mobile.whapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.textfield.TextInputEditText
import com.tlrm.mobile.whapp.mvvm.login.view.LoginActivity
import com.tlrm.mobile.whapp.mvvm.picklistscan.view.PickListScanActivity
import com.tlrm.mobile.whapp.workers.RemovePickListSyncWorker
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {

    private val SCAN_BY_CAMERA_ACTIVITY_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val scan_button = findViewById<Button>(R.id.activity_home_scan_button)
        var log_off_button = findViewById<Button>(R.id.activity_home_log_off_button)

        scan_button.setOnClickListener {
            val scanIntent = Intent(this@HomeActivity, PickListScanActivity::class.java)
            startActivityForResult(scanIntent, SCAN_BY_CAMERA_ACTIVITY_CODE)
        }

        log_off_button.setOnClickListener {
            val mainIntent = Intent(this@HomeActivity, LoginActivity::class.java)
            startActivity(mainIntent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Scan by camera activity code
        if (requestCode == SCAN_BY_CAMERA_ACTIVITY_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                val barcode_text_input = findViewById<TextInputEditText>(R.id.activity_home_barcode_text_input)
                val barcodes = data?.getStringArrayExtra("BarCodes")
                if (barcodes != null) {
                    if (barcodes.isNotEmpty()) {

                    }
                }
            }
        }
    }

}