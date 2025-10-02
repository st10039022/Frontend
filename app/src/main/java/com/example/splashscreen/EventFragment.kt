package com.example.splashscreen

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EventsFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private var upcomingRecycler: RecyclerView? = null
    private lateinit var selectedDateText: TextView
    private lateinit var noEventsDayText: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var adapter: EventsListAdapter
    private var upcomingAdapter: EventsListAdapter? = null
    private var upcomingHeader: TextView? = null

    private lateinit var btnAddEvent: Button
    private lateinit var layoutAddForm: LinearLayout
    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var etDate: EditText
    private lateinit var etStart: EditText
    private lateinit var etEnd: EditText
    private lateinit var btnSubmit: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Admin UI elements
        btnAddEvent = view.findViewById(R.id.btn_add_event)
        layoutAddForm = view.findViewById(R.id.layout_add_event_form)
        etName = view.findViewById(R.id.et_event_name)
        etDesc = view.findViewById(R.id.et_event_desc)
        etDate = view.findViewById(R.id.et_event_date)
        etStart = view.findViewById(R.id.et_event_start_time)
        etEnd = view.findViewById(R.id.et_event_end_time)
        btnSubmit = view.findViewById(R.id.btn_submit_event)

        // Recycler and views
        calendarView = view.findViewById(R.id.calendar_view)
        selectedDateText = view.findViewById(R.id.tv_selected_date)
        noEventsDayText = view.findViewById(R.id.tv_no_events_day)
        recycler = view.findViewById(R.id.rv_events)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventsListAdapter(emptyList())
        recycler.adapter = adapter

        upcomingRecycler = view.findViewById(R.id.rv_upcoming)
        upcomingHeader = view.findViewById(R.id.tv_upcoming_header)
        if (upcomingRecycler != null) {
            upcomingRecycler!!.layoutManager = LinearLayoutManager(requireContext())
            upcomingAdapter = EventsListAdapter(emptyList())
            upcomingRecycler!!.adapter = upcomingAdapter
        }

        // Check if admin
        val isAdmin = arguments?.getBoolean("isAdminMode", false) ?: false
        if (isAdmin) {
            btnAddEvent.visibility = View.VISIBLE
        } else {
            btnAddEvent.visibility = View.GONE
            layoutAddForm.visibility = View.GONE
        }

        // Toggle form visibility
        btnAddEvent.setOnClickListener {
            layoutAddForm.visibility =
                if (layoutAddForm.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // ---- DATE PICKER SETUP ----
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(),
                { _, year, month, dayOfMonth ->
                    etDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Submit new event
        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val dateStr = etDate.text.toString().trim()
            val start = etStart.text.toString().trim()
            val end = etEnd.text.toString().trim()

            if (name.isEmpty() || desc.isEmpty() || dateStr.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dateMillis = parseDateToMillis(dateStr)

            // Create event object
            val newEvent = Event(
                id = "",
                name = name,
                description = desc,
                dateMillis = dateMillis,
                startTime = start,
                endTime = end
            )

            // Save to Firestore
            db.collection("events")
                .add(newEvent)
                .addOnSuccessListener { docRef ->
                    docRef.update("id", docRef.id)
                    Toast.makeText(requireContext(), "Event added", Toast.LENGTH_SHORT).show()

                    // Clear form
                    etName.text.clear()
                    etDesc.text.clear()
                    etDate.text.clear()
                    etStart.text.clear()
                    etEnd.text.clear()
                    layoutAddForm.visibility = View.GONE

                    // Reload data
                    loadDay(dateMillis)
                    loadUpcoming(dateMillis)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val dayStart = getStartOfDayMillis(year, month, dayOfMonth)
            updateSelectedDateText(dayStart)
            loadDay(dayStart)
        }

        // Default = today
        val initCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        calendarView.date = initCal.timeInMillis
        updateSelectedDateText(initCal.timeInMillis)
        loadDay(initCal.timeInMillis)
        loadUpcoming(initCal.timeInMillis)
    }

    private fun parseDateToMillis(dateStr: String): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.parse(dateStr)
        return date?.time ?: getStartOfDayMillisFromCalendar()
    }

    private fun getStartOfDayMillisFromCalendar(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfDayMillis(year: Int, monthZeroBased: Int, day: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(year, monthZeroBased, day)
        }
        return cal.timeInMillis
    }

    private fun updateSelectedDateText(dayStartMillis: Long) {
        val fmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        selectedDateText.text = fmt.format(Date(dayStartMillis))
    }

    private fun loadDay(dayStartMillis: Long) {
        db.collection("events")
            .whereEqualTo("dateMillis", dayStartMillis)
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
                }.sortedBy { it.startTime }
                adapter.update(events)
                noEventsDayText.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun loadUpcoming(fromMillis: Long) {
        if (upcomingRecycler == null || upcomingAdapter == null) return
        db.collection("events")
            .whereGreaterThanOrEqualTo("dateMillis", fromMillis)
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
                upcomingAdapter?.update(events)
                upcomingHeader?.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            }
    }
}

// Adapter stays same
private class EventsListAdapter(
    private var items: List<Event>
) : RecyclerView.Adapter<EventsListAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_event_title)
        val subtitle: TextView = view.findViewById(R.id.tv_event_subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val e = items[position]
        val dateStr = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(e.dateMillis))
        holder.title.text = "${e.name} (${e.startTime} - ${e.endTime})"
        holder.subtitle.text = "$dateStr • ${e.description}"

        val colors = listOf(
            0xFF64B5F6.toInt(),
            0xFFF06292.toInt(),
            0xFFBA68C8.toInt()
        )
        holder.itemView.setBackgroundColor(colors[position % colors.size])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Event>) {
        items = newItems
        notifyDataSetChanged()
    }
}
