package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class DonatePaymentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the payment layout
        return inflater.inflate(R.layout.fragment_donate_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Generate QR button
        val generateQRButton: Button = view.findViewById(R.id.buttonGenerateQR)
        generateQRButton.setOnClickListener {
            Snackbar.make(view, "Generate QR clicked", Snackbar.LENGTH_SHORT).show()
        }

        // Other payment button
        val otherPaymentButton: Button = view.findViewById(R.id.buttonOtherPayment)
        otherPaymentButton.setOnClickListener {
            Snackbar.make(view, "Other Payment Methods clicked", Snackbar.LENGTH_SHORT).show()
        }
    }
}
