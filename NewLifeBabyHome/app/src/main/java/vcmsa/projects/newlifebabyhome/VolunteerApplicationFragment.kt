package vcmsa.projects.newlifebabyhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

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
        val nameEditText = view.findViewById<EditText>(R.id.et_name)
        val emailEditText = view.findViewById<EditText>(R.id.et_email)
        val whyEditText = view.findViewById<EditText>(R.id.et_why)
        val experienceEditText = view.findViewById<EditText>(R.id.et_experience)
        val submitButton = view.findViewById<Button>(R.id.btn_submit_application)
        val selectAvailabilityButton = view.findViewById<Button>(R.id.btn_select_availability)
        val selectedDatesTextView = view.findViewById<android.widget.TextView>(R.id.tv_selected_dates)

        selectAvailabilityButton.setOnClickListener {
            showDateRangePicker(selectedDatesTextView)
        }

        submitButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val why = whyEditText.text.toString().trim()
            val experience = experienceEditText.text.toString().trim()
            val availability = if (selectedStartDate != null && selectedEndDate != null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                "${sdf.format(Date(selectedStartDate!!))} to ${sdf.format(Date(selectedEndDate!!))}"
            } else {
                null
            }

            if (name.isEmpty() || email.isEmpty() || why.isEmpty() || experience.isEmpty() || availability == null) {
                Toast.makeText(requireContext(), "Please fill in all fields and select availability", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val application = hashMapOf(
                "name" to name,
                "email" to email,
                "why" to why,
                "experience" to experience,
                "availability" to availability,
                "status" to "pending"
            )

            db.collection("volunteer_applications")
                .add(application)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Application submitted!", Toast.LENGTH_LONG).show()
                    nameEditText.text.clear()
                    emailEditText.text.clear()
                    whyEditText.text.clear()
                    experienceEditText.text.clear()
                    selectedDatesTextView.text = ""
                    selectedStartDate = null
                    selectedEndDate = null
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to submit application", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showDateRangePicker(selectedDatesTextView: android.widget.TextView) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startYear = today.get(Calendar.YEAR)
        val startMonth = today.get(Calendar.MONTH)
        val startDay = today.get(Calendar.DAY_OF_MONTH)

        val startPicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val startCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(year, month, dayOfMonth)
            }
            selectedStartDate = startCal.timeInMillis

            val endPicker = DatePickerDialog(requireContext(), { _, endYear, endMonth, endDayOfMonth ->
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(endYear, endMonth, endDayOfMonth)
                }
                selectedEndDate = endCal.timeInMillis

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDatesTextView.text = "${sdf.format(Date(selectedStartDate!!))} to ${sdf.format(Date(selectedEndDate!!))}"
            }, startYear, startMonth, startDay)
            // End date cannot be before start date
            endPicker.datePicker.minDate = startCal.timeInMillis
            endPicker.show()
        }, startYear, startMonth, startDay)
        // Start date cannot be before today
        startPicker.datePicker.minDate = today.timeInMillis
        startPicker.show()
    }
}