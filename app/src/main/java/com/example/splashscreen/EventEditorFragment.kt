package com.example.splashscreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EventEditorFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private var eventId: String? = null

    private lateinit var back: ImageView
    private lateinit var titleHeader: TextView

    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var tvDate: TextView
    private lateinit var tvStart: TextView
    private lateinit var tvEnd: TextView
    private lateinit var btnSave: Button

    // Stored values
    private var selectedDateMillis: Long = 0L // start of day
    private var startTime24: String = ""      // "HH:mm"
    private var endTime24: String = ""        // "HH:mm"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event_editor, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventId = arguments?.getString("eventId")

        back = view.findViewById(R.id.iv_back)
        titleHeader = view.findViewById(R.id.tv_header)
        etName = view.findViewById(R.id.et_event_name)
        etDesc = view.findViewById(R.id.et_event_desc)
        tvDate = view.findViewById(R.id.tv_event_date)
        tvStart = view.findViewById(R.id.tv_event_start)
        tvEnd = view.findViewById(R.id.tv_event_end)
        btnSave = view.findViewById(R.id.btn_save_event)

        back.setOnClickListener { parentFragmentManager.popBackStack() }

        titleHeader.text = if (eventId == null) "Create Event" else "Edit Event"

        // Defaults
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        selectedDateMillis = todayStart
        tvDate.text = formatDate(selectedDateMillis)

        tvDate.setOnClickListener { pickDate() }
        tvStart.setOnClickListener { pickTime { hh, mm ->
            startTime24 = String.format(Locale.getDefault(), "%02d:%02d", hh, mm)
            tvStart.text = formatTimeDisplay(hh, mm)
        } }
        tvEnd.setOnClickListener { pickTime { hh, mm ->
            endTime24 = String.format(Locale.getDefault(), "%02d:%02d", hh, mm)
            tvEnd.text = formatTimeDisplay(hh, mm)
        } }

        btnSave.setOnClickListener { saveEvent() }

        // If editing, load existing
        eventId?.let { loadExisting(it) }
    }

    private fun loadExisting(id: String) {
        db.collection("events").document(id).get()
            .addOnSuccessListener { d ->
                val e = Event(
                    id = d.getString("id") ?: d.id,
                    name = d.getString("name") ?: "",
                    description = d.getString("description") ?: "",
                    dateMillis = d.getLong("dateMillis") ?: 0L,
                    startTime = d.getString("startTime") ?: "",
                    endTime = d.getString("endTime") ?: "",
                    isDeleted = d.getBoolean("isDeleted") ?: false
                )
                etName.setText(e.name)
                etDesc.setText(e.description)
                selectedDateMillis = e.dateMillis
                tvDate.text = formatDate(selectedDateMillis)
                startTime24 = e.startTime
                endTime24 = e.endTime
                tvStart.text = toDisplay(startTime24)
                tvEnd.text = toDisplay(endTime24)
            }
    }

    // ---------- Pickers ----------

    private fun pickDate() {
        val cal = Calendar.getInstance()
        if (selectedDateMillis > 0) cal.timeInMillis = selectedDateMillis
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, year, month, day ->
            val picked = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(year, month, day)
            }
            selectedDateMillis = picked.timeInMillis
            tvDate.text = formatDate(selectedDateMillis)
        }, y, m, d).show()
    }

    private fun pickTime(onPicked: (hour24: Int, minute: Int) -> Unit) {
        val cal = Calendar.getInstance()
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            onPicked(hourOfDay, minute)
        }, h, min, false).show()
    }

    // ---------- Save ----------

    private fun saveEvent() {
        val name = etName.text.toString().trim()
        val desc = etDesc.text.toString().trim()

        if (name.isEmpty() || desc.isEmpty() || selectedDateMillis == 0L || startTime24.isEmpty() || endTime24.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate time order
        val startMin = toMinutes(startTime24)
        val endMin = toMinutes(endTime24)
        if (endMin <= startMin) {
            Toast.makeText(requireContext(), "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        val id = eventId ?: db.collection("events").document().id

        val data = mapOf(
            "id" to id,
            "name" to name,
            "description" to desc,
            "dateMillis" to selectedDateMillis,
            "startTime" to startTime24,
            "endTime" to endTime24,
            "isDeleted" to false,
            "_k" to AdminSecrets.ADMIN_KEY
        )

        db.collection("events").document(id)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), if (eventId == null) "Event created" else "Event updated", Toast.LENGTH_SHORT).show()
                // tell EventsFragment to refresh, then pop back to it
                parentFragmentManager.setFragmentResult("events_changed", bundleOf("refresh" to true))
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    // ---------- Formatting helpers ----------

    private fun formatDate(millis: Long): String {
        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return fmt.format(Date(millis))
    }

    private fun formatTimeDisplay(hour24: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    private fun toDisplay(hhmm24: String): String = try {
        val p = SimpleDateFormat("HH:mm", Locale.getDefault())
        val d = p.parse(hhmm24)
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d!!)
    } catch (_: Exception) { hhmm24 }

    private fun toMinutes(hhmm24: String): Int {
        val parts = hhmm24.split(":")
        if (parts.size != 2) return 0
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }
}
