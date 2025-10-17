package com.example.splashscreen

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnGlobalBack: View

    // root tabs
    private val dashboardFragment = DashboardFragment()
    private val aboutUsFragment = AboutUsFragment()
    private val eventsFragment = EventsFragment()
    private val faqFragment = FaqFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav)
        btnGlobalBack = findViewById(R.id.btn_global_back)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, dashboardFragment)
                .commit()
            // ensure single-selection at app start
            bottomNav.menu.setGroupCheckable(0, true, true)
            bottomNav.selectedItemId = R.id.nav_home
        }

        bottomNav.setOnItemSelectedListener { item ->
            // whenever user taps a tab, restore single-selection mode
            bottomNav.menu.setGroupCheckable(0, true, true)
            val handled = when (item.itemId) {
                R.id.nav_home -> switchFragment(dashboardFragment)
                R.id.nav_location -> switchFragment(aboutUsFragment)
                R.id.nav_notifications -> switchFragment(eventsFragment)
                R.id.nav_profile -> switchFragment(faqFragment)
                else -> false
            }
            updateGlobalBackVisibility()
            handled
        }

        btnGlobalBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        supportFragmentManager.addOnBackStackChangedListener { updateGlobalBackVisibility() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val isHome = bottomNav.selectedItemId == R.id.nav_home
                val hasBackStack = supportFragmentManager.backStackEntryCount > 0
                if (!isHome || hasBackStack) {
                    supportFragmentManager.popBackStack(
                        null,
                        androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    goHome()
                } else {
                    finish()
                }
            }
        })

        updateGlobalBackVisibility()
    }

    fun selectTab(@IdRes id: Int) {
        // called from dashboard tiles/search — restore single-selection then select
        bottomNav.menu.setGroupCheckable(0, true, true)
        bottomNav.selectedItemId = id
        updateGlobalBackVisibility()
    }

    fun goHome() {
        bottomNav.menu.setGroupCheckable(0, true, true)
        bottomNav.selectedItemId = R.id.nav_home
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, dashboardFragment)
            .commit()
        updateGlobalBackVisibility()
    }

    private fun switchFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    private fun updateGlobalBackVisibility() {
        val show = bottomNav.selectedItemId != R.id.nav_home ||
                supportFragmentManager.backStackEntryCount > 0
        btnGlobalBack.visibility = if (show) View.VISIBLE else View.GONE
    }
}
