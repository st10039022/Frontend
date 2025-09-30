package com.example.splashscreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

        // Use argument if present, otherwise fall back to SessionManager flag
        isAdminMode = arguments?.getBoolean("isAdminMode") ?: SessionManager.isAdmin

        val welcomeText = view.findViewById<TextView>(R.id.textWelcomeAdmin)
        val adminLoginText = view.findViewById<TextView>(R.id.textAdminLogin)

        // show/hide welcome and set admin link text
        if (isAdminMode) {
            welcomeText.visibility = View.VISIBLE
            adminLoginText.text = "Logout Admin"
        } else {
            welcomeText.visibility = View.GONE
            adminLoginText.text = "Admin Login"
        }

        // Donate button (grid)
        view.findViewById<LinearLayout>(R.id.buttonDonateDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment())
                .addToBackStack(null)
                .commit()
        }

        // Spotlight Donate button
        view.findViewById<Button>(R.id.buttonDonateSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment())
                .addToBackStack(null)
                .commit()
        }

        // Volunteer -> admin sees admin volunteer screen, regular user sees application
        view.findViewById<LinearLayout>(R.id.buttonVolunteerDashboard).setOnClickListener {
            val frag = if (isAdminMode) {
                // use the admin volunteer fragment you provided earlier
                AdminVolunteerApplicationsFragment()
            } else {
                VolunteerApplicationFragment()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }

        // Spotlight Volunteer button (keeps original behavior)
        view.findViewById<Button>(R.id.buttonVolunteerSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VolunteerApplicationFragment())
                .addToBackStack(null)
                .commit()
        }

        // Events -> pass isAdminMode so EventsFragment can show add-event controls
        view.findViewById<LinearLayout>(R.id.buttonEventsDashboard).setOnClickListener {
            val frag = EventsFragment().apply {
                arguments = Bundle().apply { putBoolean("isAdminMode", isAdminMode) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }

        // Spotlight Events button
        view.findViewById<Button>(R.id.buttonEventsSpotlight).setOnClickListener {
            val frag = EventsFragment().apply {
                arguments = Bundle().apply { putBoolean("isAdminMode", isAdminMode) }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }

        // Testimonials
        view.findViewById<LinearLayout>(R.id.buttonTestimonialsDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TestimonialsFragment())
                .addToBackStack(null)
                .commit()
        }

        // About Us
        view.findViewById<LinearLayout>(R.id.buttonAboutDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutUsFragment())
                .addToBackStack(null)
                .commit()
        }

        // FAQ (was the user button) -> opens FaqActivity
        view.findViewById<LinearLayout>(R.id.buttonUserDashboard).setOnClickListener {
            startActivity(Intent(requireContext(), FaqActivity::class.java))
        }

        // Admin login/logout link
        adminLoginText.setOnClickListener {
            if (isAdminMode) {
                // logout: clear session and update UI
                SessionManager.isAdmin = false
                isAdminMode = false
                // update UI in place
                welcomeText.visibility = View.GONE
                adminLoginText.text = "Admin Login"
                Toast.makeText(requireContext(), "Logged out of admin", Toast.LENGTH_SHORT).show()
            } else {
                // go to admin login
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AdminLoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
