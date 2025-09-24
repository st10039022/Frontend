package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EventsFragment : Fragment() {
    private lateinit var recycler: RecyclerView
    private var upcomingRecycler: RecyclerView? = null
    private lateinit var selectedDateText: TextView
    private lateinit var noEventsDayText: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var adapter: EventsListAdapter
    private var upcomingAdapter: EventsListAdapter? = null
    private var upcomingHeader: TextView? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Cycle background colors like colleague’s design
        val colors = listOf(
            0xFF64B5F6.toInt(), // Blue
            0xFFF06292.toInt(), // Pink
            0xFFBA68C8.toInt()  // Purple
        )
        holder.itemView.setBackgroundColor(colors[position % colors.size])
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Event>) {
        items = newItems
        notifyDataSetChanged()
    }
}
