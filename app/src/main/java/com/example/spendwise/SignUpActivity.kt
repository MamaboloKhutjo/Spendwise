package com.example.spendwise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etRetypePassword = findViewById<EditText>(R.id.etRetypePassword)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)
        val btnClose = findViewById<ImageView>(R.id.btnClose)

        btnClose.setOnClickListener {
            finish()
        }

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val retypePassword = etRetypePassword.text.toString()

            when {
                name.isEmpty() -> {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                }
                password != retypePassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                !cbTerms.isChecked -> {
                    Toast.makeText(this, "Please agree to the Terms & Privacy", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    lifecycleScope.launch {
                        val success = AppData.register(name, email, password)
                        if (success) {
                            Toast.makeText(this@SignUpActivity, "Account created!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignUpActivity, DashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@SignUpActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        tvSignIn.setOnClickListener {
            finish()
        }
    }
}
