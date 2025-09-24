package com.example.splashscreen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class VolunteerApplicationFragment : Fragment() {

    private var selectedStartDate: Long? = null
    private var selectedEndDate: Long? = null
    private var selectedFileUri: Uri? = null

    private val FILE_PICK_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_volunteer_application, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        val nameEditText = view.findViewById<EditText>(R.id.et_name)
        val emailEditText = view.findViewById<EditText>(R.id.et_email)
        val whyEditText = view.findViewById<EditText>(R.id.et_why)
        val experienceEditText = view.findViewById<EditText>(R.id.et_experience)
        val submitButton = view.findViewById<Button>(R.id.btn_submit_application)
        val rlSelectAvailability = view.findViewById<RelativeLayout>(R.id.rl_select_availability)
        val selectedDatesTextView = view.findViewById<TextView>(R.id.tv_selected_dates)
        val tvSelectedFile = view.findViewById<TextView>(R.id.tv_selected_file)
        val btnUploadFile = view.findViewById<Button>(R.id.btn_upload_file)

        // Date Picker
        rlSelectAvailability.setOnClickListener {
            showDateRangePicker(selectedDatesTextView)
        }

        // File Picker
        btnUploadFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // allow any file type
            startActivityForResult(Intent.createChooser(intent, "Select file"), FILE_PICK_CODE)
        }

        // Submit Button
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

            // Prepare application map
            val application = hashMapOf<String, Any>(
                "name" to name,
                "email" to email,
                "why" to why,
                "experience" to experience,
                "availability" to availability,
                "status" to "pending"
            )

            // If file is selected, upload it first
            if (selectedFileUri != null) {
                val storageRef = storage.reference.child("volunteer_files/${UUID.randomUUID()}")
                storageRef.putFile(selectedFileUri!!)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            application["fileUrl"] = uri.toString()
                            saveApplicationToFirestore(db, application, nameEditText, emailEditText, whyEditText,
                                experienceEditText, selectedDatesTextView, tvSelectedFile)
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "File upload failed, but application can still be submitted.", Toast.LENGTH_LONG).show()
                            saveApplicationToFirestore(db, application, nameEditText, emailEditText, whyEditText,
                                experienceEditText, selectedDatesTextView, tvSelectedFile)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "File upload failed, but application can still be submitted.", Toast.LENGTH_LONG).show()
                        saveApplicationToFirestore(db, application, nameEditText, emailEditText, whyEditText,
                            experienceEditText, selectedDatesTextView, tvSelectedFile)
                    }
            } else {
                // No file selected, just save
                saveApplicationToFirestore(db, application, nameEditText, emailEditText, whyEditText,
                    experienceEditText, selectedDatesTextView, tvSelectedFile)
            }
        }
    }

    private fun saveApplicationToFirestore(
        db: FirebaseFirestore,
        application: HashMap<String, Any>,
        nameEditText: EditText,
        emailEditText: EditText,
        whyEditText: EditText,
        experienceEditText: EditText,
        selectedDatesTextView: TextView,
        tvSelectedFile: TextView
    ) {
        db.collection("volunteer_applications")
            .add(application)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Application submitted successfully!", Toast.LENGTH_LONG).show()

                // Clear form
                nameEditText.text.clear()
                emailEditText.text.clear()
                whyEditText.text.clear()
                experienceEditText.text.clear()
                selectedDatesTextView.text = ""
                tvSelectedFile.text = "Upload Here"
                selectedStartDate = null
                selectedEndDate = null
                selectedFileUri = null

                // Navigate back to DashboardFragment
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DashboardFragment())
                    .commit()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to submit application", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            selectedFileUri?.let {
                val fileName = it.lastPathSegment?.substringAfterLast("/") ?: "Selected File"
                val tvSelectedFile = view?.findViewById<TextView>(R.id.tv_selected_file)
                tvSelectedFile?.text = fileName
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

        val startPicker = android.app.DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val startCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(year, month, dayOfMonth)
            }
            selectedStartDate = startCal.timeInMillis

            val endPicker = android.app.DatePickerDialog(requireContext(), { _, endYear, endMonth, endDayOfMonth ->
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

            endPicker.datePicker.minDate = startCal.timeInMillis
            endPicker.show()
        }, startYear, startMonth, startDay)

        startPicker.datePicker.minDate = today.timeInMillis
        startPicker.show()
    }
}
