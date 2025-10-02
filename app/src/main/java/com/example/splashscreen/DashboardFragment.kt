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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var isAdminMode: Boolean = false
    private val db = FirebaseFirestore.getInstance()

    private var spotlightEventTitle: TextView? = null
    private var spotlightEventSubtitle: TextView? = null
    private var spotlightEventButton: Button? = null
    private var spotlightEvent: Event? = null

    // Button labels
    private lateinit var textDonateLabel: TextView
    private lateinit var textVolunteerLabel: TextView
    private lateinit var textEventsLabel: TextView
    private lateinit var textTestimonialsLabel: TextView
    private lateinit var textAboutLabel: TextView
    private lateinit var textFaqLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isAdminMode = arguments?.getBoolean("isAdminMode") ?: SessionManager.isAdmin

        val welcomeText = view.findViewById<TextView>(R.id.textWelcomeAdmin)
        val adminLoginText = view.findViewById<TextView>(R.id.textAdminLogin)

        // Spotlight event views
        spotlightEventTitle = view.findViewById(R.id.tvSpotlightEventTitle)
        spotlightEventSubtitle = view.findViewById(R.id.tvSpotlightEventSubtitle)
        spotlightEventButton = view.findViewById(R.id.buttonEventsSpotlight)

        // Grid button labels
        textDonateLabel = view.findViewById(R.id.textDonateLabel)
        textVolunteerLabel = view.findViewById(R.id.textVolunteerLabel)
        textEventsLabel = view.findViewById(R.id.textEventsLabel)
        textTestimonialsLabel = view.findViewById(R.id.textTestimonialsLabel)
        textAboutLabel = view.findViewById(R.id.textAboutLabel)
        textFaqLabel = view.findViewById(R.id.textFaqLabel)

        // Load spotlight event
        loadSpotlightEvent()

        // show/hide welcome and set admin link text
        if (isAdminMode) {
            welcomeText.visibility = View.VISIBLE
            adminLoginText.text = "Logout Admin"

            // Change all button labels for admin
            textDonateLabel.text = "Manage Donations"
            textVolunteerLabel.text = "Manage Volunteers"
            textEventsLabel.text = "Manage Events"
            textTestimonialsLabel.text = "Manage Testimonials"
            textAboutLabel.text = "Manage About Us"
            textFaqLabel.text = "Manage FAQ"
        } else {
            welcomeText.visibility = View.GONE
            adminLoginText.text = "Admin Login"

            // Reset user labels
            textDonateLabel.text = "Donate"
            textVolunteerLabel.text = "Volunteer"
            textEventsLabel.text = "Events"
            textTestimonialsLabel.text = "Testimonials"
            textAboutLabel.text = "About Us"
            textFaqLabel.text = "FAQ"
        }

        // Donate button
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

        // Volunteer button
        view.findViewById<LinearLayout>(R.id.buttonVolunteerDashboard).setOnClickListener {
            val frag = if (isAdminMode) {
                AdminVolunteerApplicationsFragment()
            } else {
                VolunteerApplicationFragment()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, frag)
                .addToBackStack(null)
                .commit()
        }

        // Spotlight Volunteer
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

        // Spotlight Events button
        spotlightEventButton?.setOnClickListener {
            spotlightEvent?.let { e ->
                val frag = EventDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putString("id", e.id)
                        putString("name", e.name)
                        putString("description", e.description)
                        putLong("dateMillis", e.dateMillis)
                        putString("startTime", e.startTime)
                        putString("endTime", e.endTime)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .addToBackStack(null)
                    .commit()
            } ?: run {
                Toast.makeText(requireContext(), "No upcoming event", Toast.LENGTH_SHORT).show()
            }
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

        // FAQ
        view.findViewById<LinearLayout>(R.id.buttonUserDashboard).setOnClickListener {
            startActivity(Intent(requireContext(), FaqActivity::class.java))
        }

        // Admin login/logout link
        adminLoginText.setOnClickListener {
            if (isAdminMode) {
                SessionManager.isAdmin = false
                isAdminMode = false
                welcomeText.visibility = View.GONE
                adminLoginText.text = "Admin Login"

                // Reset button labels
                textDonateLabel.text = "Donate"
                textVolunteerLabel.text = "Volunteer"
                textEventsLabel.text = "Events"
                textTestimonialsLabel.text = "Testimonials"
                textAboutLabel.text = "About Us"
                textFaqLabel.text = "FAQ"

                Toast.makeText(requireContext(), "Logged out of admin", Toast.LENGTH_SHORT).show()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AdminLoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun loadSpotlightEvent() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        db.collection("events")
            .whereGreaterThanOrEqualTo("dateMillis", today)
            .get()
            .addOnSuccessListener { snap ->
                val events = snap.documents.map { d ->
                    Event(
                        id = d.id,
                        name = d.getString("name") ?: "",
                        description = d.getString("description") ?: "",
                        dateMillis = d.getLong("dateMillis") ?: 0L,
                        startTime = d.getString("startTime") ?: "",
                        endTime = d.getString("endTime") ?: ""
                    )
                }.sortedWith(compareBy<Event> { it.dateMillis }.thenBy { it.startTime })

                if (events.isNotEmpty()) {
                    spotlightEvent = events.first()
                    val fmt = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
                    spotlightEventTitle?.text = spotlightEvent!!.name
                    spotlightEventSubtitle?.text =
                        "${fmt.format(Date(spotlightEvent!!.dateMillis))} • ${spotlightEvent!!.startTime} - ${spotlightEvent!!.endTime}"
                } else {
                    spotlightEventTitle?.text = "No upcoming events"
                    spotlightEventSubtitle?.text = ""
                    spotlightEvent = null
                }
            }
    }
}
