package com.example.splashscreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    // Fragments for bottom nav
    private val dashboardFragment = DashboardFragment()
    private val aboutUsFragment = AboutUsFragment()
    private val eventsFragment = EventsFragment()
    // Removed faqFragment because FAQ is an Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)

        // Load default fragment (Dashboard)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, dashboardFragment)
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(dashboardFragment)
                R.id.nav_location -> switchFragment(aboutUsFragment)
                R.id.nav_notifications -> switchFragment(eventsFragment)
                R.id.nav_profile -> {
                    // Launch FAQ activity
                    val intent = Intent(this, FaqActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}
