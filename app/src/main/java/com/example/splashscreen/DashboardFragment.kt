package com.example.splashscreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {

    private var isAdminMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isAdminMode = arguments?.getBoolean("isAdminMode", false) ?: false

        val welcomeText = view.findViewById<TextView>(R.id.textWelcomeAdmin)
        val adminLoginText = view.findViewById<TextView>(R.id.textAdminLogin)

        if (isAdminMode) {
            welcomeText.visibility = View.VISIBLE
            adminLoginText.text = "Logout Admin"
        } else {
            welcomeText.visibility = View.GONE
            adminLoginText.text = "Admin Login"
        }

        // Donate button
        view.findViewById<LinearLayout>(R.id.buttonDonateDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<Button>(R.id.buttonDonateSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment())
                .addToBackStack(null)
                .commit()
        }

        // Volunteer button
        view.findViewById<LinearLayout>(R.id.buttonVolunteerDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VolunteerApplicationFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<Button>(R.id.buttonVolunteerSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VolunteerApplicationFragment())
                .addToBackStack(null)
                .commit()
        }

        // Events button
        view.findViewById<LinearLayout>(R.id.buttonEventsDashboard).setOnClickListener {
            val frag = EventsFragment().apply {
                arguments = Bundle().apply { putBoolean("isAdminMode", isAdminMode) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<Button>(R.id.buttonEventsSpotlight).setOnClickListener {
            val frag = EventsFragment().apply {
                arguments = Bundle().apply { putBoolean("isAdminMode", isAdminMode) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }

        // Testimonials button
        view.findViewById<LinearLayout>(R.id.buttonTestimonialsDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TestimonialsFragment())
                .addToBackStack(null)
                .commit()
        }

        // About Us button
        view.findViewById<LinearLayout>(R.id.buttonAboutDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutUsFragment())
                .addToBackStack(null)
                .commit()
        }

        // FAQ button (replaces previous User button)
        view.findViewById<LinearLayout>(R.id.buttonUserDashboard).setOnClickListener {
            startActivity(Intent(requireContext(), FaqActivity::class.java))
        }

        // Admin login/logout
        adminLoginText.setOnClickListener {
            if (isAdminMode) {
                val frag = DashboardFragment().apply {
                    arguments = Bundle().apply { putBoolean("isAdminMode", false) }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .commit()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AdminLoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
