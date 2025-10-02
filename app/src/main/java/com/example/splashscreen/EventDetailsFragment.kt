package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("name") ?: ""
        val desc = arguments?.getString("description") ?: ""
        val dateMillis = arguments?.getLong("dateMillis") ?: 0L
        val start = arguments?.getString("startTime") ?: ""
        val end = arguments?.getString("endTime") ?: ""

        val tvName = view.findViewById<TextView>(R.id.tvEventName)
        val tvDate = view.findViewById<TextView>(R.id.tvEventDate)
        val tvTime = view.findViewById<TextView>(R.id.tvEventTime)
        val tvDesc = view.findViewById<TextView>(R.id.tvEventDesc)

        tvName.text = name
        tvDesc.text = desc

        val fmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        tvDate.text = fmt.format(Date(dateMillis))
        tvTime.text = "$start - $end"
    }
}
