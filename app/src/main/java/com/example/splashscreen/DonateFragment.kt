package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class DonateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the correct layout (with 3 cards)
        return inflater.inflate(R.layout.fragment_donate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Proceed to payment
        val proceedButton: Button = view.findViewById(R.id.buttonProceedDonation)
        proceedButton.setOnClickListener {
            // Navigate to DonatePaymentFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonatePaymentFragment())
                .addToBackStack(null)
                .commit()
        }

        // Drop-off zones
        val dropOffButton: Button = view.findViewById(R.id.buttonDropOffZones)
        dropOffButton.setOnClickListener {
            // Navigate to DropOffZonesFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DropOffZonesFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
