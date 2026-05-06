package com.example.spendwise

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)

        val handler = Handler(Looper.getMainLooper())
        
        handler.postDelayed({
            dot1.setBackgroundResource(R.drawable.dot_inactive)
            dot2.setBackgroundResource(R.drawable.dot_active)
        }, 500)

        handler.postDelayed({
            dot2.setBackgroundResource(R.drawable.dot_inactive)
            dot3.setBackgroundResource(R.drawable.dot_active)
        }, 1000)

        handler.postDelayed({
            dot3.setBackgroundResource(R.drawable.dot_inactive)
            dot1.setBackgroundResource(R.drawable.dot_active)
        }, 1500)

        handler.postDelayed({
            // Check if user is already logged in
            val intent = if (AppData.currentUser != null) {
                Intent(this, DashboardActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000)
    }
}
