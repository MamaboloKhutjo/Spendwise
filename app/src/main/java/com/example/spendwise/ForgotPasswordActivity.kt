package com.example.spendwise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val btnResetPassword = findViewById<Button>(R.id.btnResetPassword)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        val btnClose = findViewById<ImageView>(R.id.btnClose)

        btnClose.setOnClickListener {
            finish()
        }

        btnResetPassword.setOnClickListener {
            Toast.makeText(this, "Reset link sent!", Toast.LENGTH_SHORT).show()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}
