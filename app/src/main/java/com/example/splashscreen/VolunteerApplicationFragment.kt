package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VolunteerApplicationFragment : Fragment() {

    private var selectedStartDate: Long? = null
    private var selectedEndDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_volunteer_application, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()

        val backBtn = view.findViewById<ImageView>(R.id.iv_back)
        val nameEditText = view.findViewById<EditText>(R.id.et_name)
        val emailEditText = view.findViewById<EditText>(R.id.et_email)
        val whyEditText = view.findViewById<EditText>(R.id.et_why)
        val experienceEditText = view.findViewById<EditText>(R.id.et_experience)
        val submitButton = view.findViewById<Button>(R.id.btn_submit_application)
        val rlSelectAvailability = view.findViewById<RelativeLayout>(R.id.rl_select_availability)
        val selectedDatesTextView = view.findViewById<TextView>(R.id.tv_selected_dates)

        // Back button (dashboard remains without back as requested)
        backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Date Range Picker
        rlSelectAvailability.setOnClickListener {
            showDateRangePicker(selectedDatesTextView)
        }

        // Submit
        submitButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val why = whyEditText.text.toString().trim()
            val experience = experienceEditText.text.toString().trim()
            val availability = if (selectedStartDate != null && selectedEndDate != null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                "${sdf.format(Date(selectedStartDate!!))} to ${sdf.format(Date(selectedEndDate!!))}"
            } else null

            if (name.isEmpty() || email.isEmpty() || why.isEmpty() || experience.isEmpty() || availability == null) {
                Toast.makeText(
                    requireContext(),
                    "Please fill all fields and select availability",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Create document with generated id and include id field in data
            val docRef = db.collection("volunteer_applications").document()
            val application = mapOf(
                "id" to docRef.id,
                "name" to name,
                "email" to email,
                "why" to why,
                "experience" to experience,
                "availability" to availability,
                "status" to "pending"
            )

            docRef.set(application)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Application submitted successfully!", Toast.LENGTH_LONG).show()

                    // Clear form
                    nameEditText.text.clear()
                    emailEditText.text.clear()
                    whyEditText.text.clear()
                    experienceEditText.text.clear()
                    selectedDatesTextView.text = ""
                    selectedStartDate = null
                    selectedEndDate = null

                    // Navigate back to DashboardFragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DashboardFragment())
                        .commit()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to submit application", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showDateRangePicker(selectedDatesTextView: TextView) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startYear = today.get(Calendar.YEAR)
        val startMonth = today.get(Calendar.MONTH)
        val startDay = today.get(Calendar.DAY_OF_MONTH)

        val startPicker = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val startCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(year, month, dayOfMonth)
                }
                selectedStartDate = startCal.timeInMillis

                val endPicker = android.app.DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDayOfMonth ->
                        val endCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            set(endYear, endMonth, endDayOfMonth)
                        }
                        selectedEndDate = endCal.timeInMillis

                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        selectedDatesTextView.text =
                            "${sdf.format(Date(selectedStartDate!!))} to ${sdf.format(Date(selectedEndDate!!))}"
                    },
                    startYear, startMonth, startDay
                )

                endPicker.datePicker.minDate = startCal.timeInMillis
                endPicker.show()
            },
            startYear, startMonth, startDay
        )

        startPicker.datePicker.minDate = today.timeInMillis
        startPicker.show()
    }
}
