package com.example.splashscreen

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DonateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        val donateButton: Button = findViewById(R.id.buttonDonate)
        donateButton.setOnClickListener {
            // TODO: Implement actual donation process or redirect to payment gateway
        }

        val dropOffButton: Button = findViewById(R.id.buttonDropOffZones)
        dropOffButton.setOnClickListener {
            // TODO: Show drop-off locations or open map intent
        }
    }
}
