package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        val back = view.findViewById<ImageView>(R.id.iv_back)
        back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val name = arguments?.getString("name") ?: ""
        val desc = arguments?.getString("description") ?: ""
        val dateMillis = arguments?.getLong("dateMillis") ?: 0L
        val start24 = arguments?.getString("startTime") ?: ""
        val end24 = arguments?.getString("endTime") ?: ""

        val tvName = view.findViewById<TextView>(R.id.tvEventName)
        val tvDate = view.findViewById<TextView>(R.id.tvEventDate)
        val tvTime = view.findViewById<TextView>(R.id.tvEventTime)
        val tvDesc = view.findViewById<TextView>(R.id.tvEventDesc)

        tvName.text = name
        tvDesc.text = desc

        val dateFmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        tvDate.text = dateFmt.format(Date(dateMillis))

        // Display times as "hh:mm a" while you store "HH:mm"
        fun hhmmA(hhmm24: String): String {
            return try {
                val p = SimpleDateFormat("HH:mm", Locale.getDefault())
                val d = p.parse(hhmm24)
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d!!)
            } catch (_: Exception) {
                hhmm24 // fallback
            }
        }
        val startDisp = hhmmA(start24)
        val endDisp = hhmmA(end24)
        tvTime.text = "$startDisp - $endDisp"
    }
}
