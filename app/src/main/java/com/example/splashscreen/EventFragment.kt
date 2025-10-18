package com.example.splashscreen

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
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

    private val db = FirebaseFirestore.getInstance()
    private var isAdminMode: Boolean = false
    private var currentSelectedDayStart: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_event, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // read arg if present, otherwise fall back to global session flag
        isAdminMode = arguments?.getBoolean("isAdminMode", SessionManager.isAdmin)
            ?: SessionManager.isAdmin

        btnAddEvent = view.findViewById(R.id.btn_add_event)
        calendarView = view.findViewById(R.id.calendar_view)
        selectedDateText = view.findViewById(R.id.tv_selected_date)
        noEventsDayText = view.findViewById(R.id.tv_no_events_day)

        recycler = view.findViewById(R.id.rv_events)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventsListAdapter(
            items = emptyList(),
            isAdmin = isAdminMode,
            onView = { e -> openDetails(e) },
            onEdit = { e -> openEditor(e.id) },
            onDelete = { e -> confirmDelete(e) }
        )
        recycler.adapter = adapter

        upcomingRecycler = view.findViewById(R.id.rv_upcoming)
        upcomingHeader = view.findViewById(R.id.tv_upcoming_header)
        if (upcomingRecycler != null) {
            upcomingRecycler!!.layoutManager = LinearLayoutManager(requireContext())
            upcomingAdapter = EventsListAdapter(
                items = emptyList(),
                isAdmin = isAdminMode,
                onView = { e -> openDetails(e) },
                onEdit = { e -> openEditor(e.id) },
                onDelete = { e -> confirmDelete(e) }
            )
            upcomingRecycler!!.adapter = upcomingAdapter
        }

        btnAddEvent.visibility = if (isAdminMode) View.VISIBLE else View.GONE
        btnAddEvent.setOnClickListener { openEditor(null) } // null => create

        // Listen for editor result to refresh immediately after save
        parentFragmentManager.setFragmentResultListener("events_changed", viewLifecycleOwner) { _, _ ->
            loadDay(currentSelectedDayStart)
            loadUpcoming(currentSelectedDayStart)
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            currentSelectedDayStart = getStartOfDayMillis(year, month, dayOfMonth)
            updateSelectedDateText(currentSelectedDayStart)
            loadDay(currentSelectedDayStart)
        }

        val initCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        calendarView.date = initCal.timeInMillis
        currentSelectedDayStart = initCal.timeInMillis
        updateSelectedDateText(currentSelectedDayStart)

        loadDay(currentSelectedDayStart)
        loadUpcoming(currentSelectedDayStart)
    }

    override fun onResume() {
        super.onResume()
        val newAdmin = SessionManager.isAdmin
        if (newAdmin != isAdminMode) {
            isAdminMode = newAdmin
            btnAddEvent.visibility = if (isAdminMode) View.VISIBLE else View.GONE
            adapter.setAdmin(isAdminMode)
            upcomingAdapter?.setAdmin(isAdminMode)
        }
        if (currentSelectedDayStart == 0L) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            currentSelectedDayStart = cal.timeInMillis
        }
        loadDay(currentSelectedDayStart)
        loadUpcoming(currentSelectedDayStart)
    }

    // -------- navigation --------

    private fun openDetails(e: Event) {
        val b = Bundle().apply {
            putString("name", e.name)
            putString("description", e.description)
            putLong("dateMillis", e.dateMillis)
            putString("startTime", e.startTime)
            putString("endTime", e.endTime)
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EventDetailsFragment().apply { arguments = b })
            .addToBackStack(null)
            .commit()
    }

    private fun openEditor(eventId: String?) {
        val b = Bundle().apply { if (eventId != null) putString("eventId", eventId) }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, EventEditorFragment().apply { arguments = b })
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDelete(e: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete event")
            .setMessage("Are you sure you want to delete “${e.name}”?")
            .setPositiveButton("Delete") { d, _ ->
                // Soft delete: include _k; rules block hard deletes
                val payload = mapOf(
                    "id" to e.id,
                    "name" to e.name,
                    "description" to e.description,
                    "dateMillis" to e.dateMillis,
                    "startTime" to e.startTime,
                    "endTime" to e.endTime,
                    "isDeleted" to true,
                    "_k" to AdminSecrets.ADMIN_KEY
                )
                FirebaseFirestore.getInstance().collection("events").document(e.id).set(payload)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                        loadDay(currentSelectedDayStart)
                        loadUpcoming(currentSelectedDayStart)
                    }
                    .addOnFailureListener { ex ->
                        Toast.makeText(requireContext(), "Failed: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                d.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    // -------- loading --------

    private fun updateSelectedDateText(dayStartMillis: Long) {
        val fmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        selectedDateText.text = fmt.format(Date(dayStartMillis))
    }

    private fun loadDay(dayStartMillis: Long) {
        db.collection("events")
            .whereEqualTo("dateMillis", dayStartMillis)
            .get()
            .addOnSuccessListener { snap ->
                val events = snap.documents.mapNotNull { d ->
                    val isDeleted = d.getBoolean("isDeleted") ?: false
                    if (isDeleted) null else Event(
                        id = d.getString("id") ?: d.id,
                        name = d.getString("name") ?: "",
                        description = d.getString("description") ?: "",
                        dateMillis = d.getLong("dateMillis") ?: 0L,
                        startTime = d.getString("startTime") ?: "",
                        endTime = d.getString("endTime") ?: "",
                        isDeleted = false
                    )
                }.sortedWith(compareBy<Event> { it.startTime }.thenBy { it.name })
                adapter.update(events)
                noEventsDayText.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun loadUpcoming(fromMillis: Long) {
        if (upcomingRecycler == null || upcomingAdapter == null) return
        db.collection("events")
            .whereGreaterThanOrEqualTo("dateMillis", fromMillis)
            .orderBy("dateMillis") // safe single-field index
            .limit(20)
            .get()
            .addOnSuccessListener { snap ->
                val events = snap.documents.mapNotNull { d ->
                    val isDeleted = d.getBoolean("isDeleted") ?: false
                    if (isDeleted) null else Event(
                        id = d.getString("id") ?: d.id,
                        name = d.getString("name") ?: "",
                        description = d.getString("description") ?: "",
                        dateMillis = d.getLong("dateMillis") ?: 0L,
                        startTime = d.getString("startTime") ?: "",
                        endTime = d.getString("endTime") ?: "",
                        isDeleted = false
                    )
                }.sortedWith(compareBy<Event> { it.dateMillis }.thenBy { it.startTime })
                upcomingAdapter?.update(events)
                upcomingHeader?.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            }
    }

    // -------- utils --------

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
}

// ---------- adapter (with admin toggle) ----------

private class EventsListAdapter(
    private var items: List<Event>,
    private var isAdmin: Boolean,
    private val onView: (Event) -> Unit,
    private val onEdit: (Event) -> Unit,
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<EventsListAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_event_title)
        val subtitle: TextView = view.findViewById(R.id.tv_event_subtitle)
        val btnEdit: ImageView = view.findViewById(R.id.iv_edit)
        val btnDelete: ImageView = view.findViewById(R.id.iv_delete)
        val card: CardView = view as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val e = items[position]
        val dateStr = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(e.dateMillis))

        fun disp(hhmm24: String): String = try {
            val p = SimpleDateFormat("HH:mm", Locale.getDefault())
            val d = p.parse(hhmm24)
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d!!)
        } catch (_: Exception) { hhmm24 }

        holder.title.text = "${e.name} (${disp(e.startTime)} - ${disp(e.endTime)})"
        holder.subtitle.text = "$dateStr • ${e.description}"

        val colors = intArrayOf(0xFF64B5F6.toInt(), 0xFFF06292.toInt(), 0xFFBA68C8.toInt())
        holder.card.setCardBackgroundColor(colors[position % colors.size])

        holder.itemView.setOnClickListener { onView(e) }

        if (isAdmin) {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEdit(e) }
            holder.btnDelete.setOnClickListener { onDelete(e) }
        } else {
            holder.btnEdit.visibility = View.GONE
            holder.btnDelete.visibility = View.GONE
            holder.btnEdit.setOnClickListener(null)
            holder.btnDelete.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Event>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setAdmin(value: Boolean) {
        if (isAdmin != value) {
            isAdmin = value
            notifyDataSetChanged()
        }
    }
}
