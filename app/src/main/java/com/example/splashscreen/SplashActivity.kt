package com.example.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay 1 second then move to DummyActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DummyActivity::class.java))
            finish() // Close splash activity
        }, 2000) // 2000ms = 2 seconds
    }
}
