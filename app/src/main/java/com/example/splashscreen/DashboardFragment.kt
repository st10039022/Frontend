package com.example.splashscreen

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var isAdminMode: Boolean = false
    private val db = FirebaseFirestore.getInstance()

    private var spotlightEventTitle: TextView? = null
    private var spotlightEventSubtitle: TextView? = null
    private var spotlightEventButton: Button? = null
    private var spotlightEvent: Event? = null

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
    ): View? = inflater.inflate(R.layout.fragment_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isAdminMode = arguments?.getBoolean("isAdminMode") ?: SessionManager.isAdmin

        val welcomeText = view.findViewById<TextView>(R.id.textWelcomeAdmin)
        val adminLoginText = view.findViewById<TextView>(R.id.textAdminLogin)

        spotlightEventTitle = view.findViewById(R.id.tvSpotlightEventTitle)
        spotlightEventSubtitle = view.findViewById(R.id.tvSpotlightEventSubtitle)
        spotlightEventButton = view.findViewById(R.id.buttonEventsSpotlight)

        textDonateLabel = view.findViewById(R.id.textDonateLabel)
        textVolunteerLabel = view.findViewById(R.id.textVolunteerLabel)
        textEventsLabel = view.findViewById(R.id.textEventsLabel)
        textTestimonialsLabel = view.findViewById(R.id.textTestimonialsLabel)
        textAboutLabel = view.findViewById(R.id.textAboutLabel)
        textFaqLabel = view.findViewById(R.id.textFaqLabel)

        // hide local bottom nav if present (prevents double nav)
        view.findViewById<BottomNavigationView?>(R.id.bottomNavigationView)?.visibility = View.GONE

        loadSpotlightEvent()

        if (isAdminMode) {
            welcomeText.visibility = View.VISIBLE
            adminLoginText.text = "Logout Admin"

            textDonateLabel.text = "Manage Donations"
            textVolunteerLabel.text = "Manage Volunteers"
            textEventsLabel.text = "Manage Events"
            textTestimonialsLabel.text = "Manage Testimonials"
            textAboutLabel.text = "Manage About Us"
            textFaqLabel.text = "Manage FAQ"
        } else {
            welcomeText.visibility = View.GONE
            adminLoginText.text = "Admin Login"

            textDonateLabel.text = "Donate"
            textVolunteerLabel.text = "Volunteer"
            textEventsLabel.text = "Events"
            textTestimonialsLabel.text = "Testimonials"
            textAboutLabel.text = "About Us"
            textFaqLabel.text = "FAQ"
        }

        // donate -> open screen and clear bottom nav highlight
        view.findViewById<LinearLayout>(R.id.buttonDonateDashboard).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment())
                .addToBackStack(null)
                .commit()
            clearBottomNavSelection()
        }
        view.findViewById<Button>(R.id.buttonDonateSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment())
                .addToBackStack(null)
                .commit()
            clearBottomNavSelection()
        }

        // volunteer -> open screen and clear highlight
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
            clearBottomNavSelection()
        }
        view.findViewById<Button>(R.id.buttonVolunteerSpotlight).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VolunteerApplicationFragment())
                .addToBackStack(null)
                .commit()
            clearBottomNavSelection()
        }

        // events tile -> switch tab so nav shows "Events"
        view.findViewById<LinearLayout>(R.id.buttonEventsDashboard).setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_notifications)
        }

        // spotlight button -> event details (secondary) -> clear highlight
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
                clearBottomNavSelection()
            } ?: Toast.makeText(requireContext(), "No upcoming event", Toast.LENGTH_SHORT).show()
        }

        // testimonials -> secondary -> clear highlight
        view.findViewById<LinearLayout>(R.id.buttonTestimonialsDashboard).setOnClickListener {
            if (isAdminMode) {
                val opened = openAnyFragment(
                    "com.example.splashscreen.ManageTestimonialsFragment",
                    "com.example.splashscreen.AdminTestimonialsFragment"
                )
                if (!opened) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TestimonialsFragment())
                        .addToBackStack(null)
                        .commit()
                }
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, TestimonialsFragment())
                    .addToBackStack(null)
                    .commit()
            }
            clearBottomNavSelection()
        }

        // about -> top tab
        view.findViewById<LinearLayout>(R.id.buttonAboutDashboard).setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_location)
        }

        // FAQ tile -> top tab
        view.findViewById<LinearLayout>(R.id.buttonUserDashboard).setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_profile)
        }

        // admin login/logout
        val adminText = adminLoginText
        adminText.setOnClickListener {
            if (isAdminMode) {
                SessionManager.isAdmin = false
                isAdminMode = false
                view.findViewById<TextView>(R.id.textWelcomeAdmin).visibility = View.GONE
                adminText.text = "Admin Login"

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
                clearBottomNavSelection()
            }
        }

        // read more
        view.findViewById<Button>(R.id.buttonReadMoreNLBH)?.setOnClickListener {
            openPdfFromAssets("nlbh.pdf")
            clearBottomNavSelection()
        }

        // search routing -> reflect tabs when applicable, otherwise clear
        val editSearch = view.findViewById<EditText>(R.id.editSearch)
        editSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: return
                if (q.length < 3) return
                fun clearText() = editSearch.setText("")

                when {
                    q.contains("donat") -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, DonateFragment())
                            .addToBackStack(null)
                            .commit()
                        clearBottomNavSelection()
                        clearText()
                    }
                    q.contains("volun") -> {
                        val frag = if (isAdminMode) {
                            AdminVolunteerApplicationsFragment()
                        } else {
                            VolunteerApplicationFragment()
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, frag)
                            .addToBackStack(null)
                            .commit()
                        clearBottomNavSelection()
                        clearText()
                    }
                    q.contains("event") -> {
                        (activity as? MainActivity)?.selectTab(R.id.nav_notifications)
                        clearText()
                    }
                    q.contains("faq") -> {
                        (activity as? MainActivity)?.selectTab(R.id.nav_profile)
                        clearText()
                    }
                    q.contains("about") || q.contains("nlbh") -> {
                        (activity as? MainActivity)?.selectTab(R.id.nav_location)
                        clearText()
                    }
                    q.contains("drop") || q.contains("map") || q.contains("direction") -> {
                        val opened = openAnyFragment(
                            "com.example.splashscreen.DropOffFragment",
                            "com.example.splashscreen.DropOffLocationFragment",
                            "com.example.splashscreen.DropoffFragment",
                            "com.example.splashscreen.DropOffZoneFragment"
                        )
                        if (!opened) {
                            Toast.makeText(requireContext(), "Drop-off screen not found", Toast.LENGTH_SHORT).show()
                        }
                        clearBottomNavSelection()
                        clearText()
                    }
                    q.contains("wish") -> {
                        val opened = openAnyFragment(
                            "com.example.splashscreen.WishlistFragment",
                            "com.example.splashscreen.ManageWishlistFragment",
                            "com.example.splashscreen.PublicWishlistFragment"
                        )
                        if (!opened) {
                            Toast.makeText(requireContext(), "Wishlist screen not found", Toast.LENGTH_SHORT).show()
                        }
                        clearBottomNavSelection()
                        clearText()
                    }
                    q.contains("testi") -> {
                        if (isAdminMode) {
                            val opened = openAnyFragment(
                                "com.example.splashscreen.ManageTestimonialsFragment",
                                "com.example.splashscreen.AdminTestimonialsFragment"
                            )
                            if (!opened) {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, TestimonialsFragment())
                                    .addToBackStack(null)
                                    .commit()
                            }
                        } else {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, TestimonialsFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                        clearBottomNavSelection()
                        clearText()
                    }
                }
            }
        })
    }

    private fun openAnyFragment(vararg fqcn: String): Boolean {
        for (name in fqcn) {
            try {
                val clazz = Class.forName(name).asSubclass(Fragment::class.java)
                val frag = clazz.getDeclaredConstructor().newInstance()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .addToBackStack(null)
                    .commit()
                return true
            } catch (_: Throwable) { }
        }
        return false
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

    private fun openPdfFromAssets(assetName: String) {
        val ctx = requireContext()
        try {
            val outFile = File(ctx.cacheDir, assetName)
            ctx.assets.open(assetName).use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            }

            val uri = FileProvider.getUriForFile(
                ctx,
                "${ctx.packageName}.fileprovider",
                outFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (intent.resolveActivity(ctx.packageManager) != null) {
                startActivity(intent)
            } else {
                startActivity(Intent(ctx, PdfViewerActivity::class.java).apply {
                    putExtra("pdfFileName", assetName)
                })
            }
        } catch (e: Exception) {
            try {
                startActivity(Intent(ctx, PdfViewerActivity::class.java).apply {
                    putExtra("pdfFileName", assetName)
                })
            } catch (_: Exception) {
                Toast.makeText(ctx, "Unable to open PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // uncheck all bottom nav items (allow none selected)
    private fun clearBottomNavSelection() {
        val bnv = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav) ?: return
        val menu = bnv.menu
        menu.setGroupCheckable(0, true, false)
        for (i in 0 until menu.size()) menu.getItem(i).isChecked = false
    }
}
