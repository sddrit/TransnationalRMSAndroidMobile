package com.tlrm.mobile.whapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val username_edittext_layout = findViewById<TextInputLayout>(R.id.activity_main_username_edit_text_layout)
        val username_edit_text = findViewById<TextInputEditText>(R.id.activity_main_username_edit_text)
        var password_editext_layout = findViewById<TextInputLayout>(R.id.activity_main_password_edit_text_layout)
        var password_edit_text = findViewById<TextInputEditText>(R.id.activity_main_password_edit_text)

        //Todo This code should execute when enviroment is development
        username_edit_text.setText("admin")
        password_edit_text.setText("1234Qwer@")

        //UserName Validation
        username_edit_text.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    username_edittext_layout.error = "Username is required"
                }else {
                    username_edittext_layout.error = null
                }
            }
        })

        //Password Validation
        password_edit_text.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    password_editext_layout.error = "Password is required"
                }else {
                    password_editext_layout.error = null
                }
            }
        })

        //Login Button

        val login_button = findViewById<Button>(R.id.activity_main_login_button)
        login_button.setOnClickListener {

            if(username_edit_text.text.isNullOrEmpty()) {
                username_edittext_layout.error = "Username is required"
                username_edittext_layout.requestFocus()
                return@setOnClickListener
            }else {
                username_edittext_layout.error = null
            }

            if(password_edit_text.text.isNullOrEmpty()) {
                password_editext_layout.error = "Password is required"
                password_editext_layout.requestFocus()
                return@setOnClickListener
            }else {
                password_editext_layout.error = null
            }

            //Validation Credentials
            val username = username_edit_text.text.toString()
            val password = password_edit_text.text.toString()

            if(username.uppercase() == "ADMIN" && password.uppercase() == "1234QWER@") {
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }else {
                val toast = Toast.makeText(this@MainActivity, "Invalid credentials, Please try again",
                    Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }


}