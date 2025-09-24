package com.example.splashscreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Volunteer -> opens VolunteerApplicationFragment inside container
        view.findViewById<LinearLayout>(R.id.buttonVolunteerDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VolunteerApplicationFragment())
                .addToBackStack(null)
                .commit()
        }

        // Spotlight Volunteer button
        view.findViewById<Button>(R.id.buttonVolunteerSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VolunteerApplicationFragment())
                .addToBackStack(null)
                .commit()
        }

        // Events -> opens EventsFragment
        view.findViewById<LinearLayout>(R.id.buttonEventsDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EventsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Spotlight Events button
        view.findViewById<Button>(R.id.buttonEventsSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EventsFragment())
                .addToBackStack(null)
                .commit()
        }

        // User Profile
        view.findViewById<LinearLayout>(R.id.buttonUserDashboard).setOnClickListener {
            // startActivity(Intent(requireContext(), UserProfileActivity::class.java))
        }

        // Testimonials
        view.findViewById<LinearLayout>(R.id.buttonTestimonialsDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TestimonialsFragment())
                .addToBackStack(null)
                .commit()
        }

        // About Us -> opens AboutUsFragment
        view.findViewById<LinearLayout>(R.id.buttonAboutDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AboutUsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
