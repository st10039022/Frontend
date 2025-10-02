package com.example.splashscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class DonatePaymentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_donate_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        val backBtn = view.findViewById<ImageView>(R.id.buttonBack)
        backBtn?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Zapper/EFT icon → show BabyChino QR
        val zapperIcon = view.findViewById<ImageView>(R.id.imageZapper)
        zapperIcon?.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Scan BabyChino")

            val qrView = ImageView(requireContext())
            qrView.setImageResource(R.drawable.babychino_qr)
            qrView.adjustViewBounds = true
            qrView.setPadding(32, 32, 32, 32)
            qrView.scaleType = ImageView.ScaleType.FIT_CENTER

            builder.setView(qrView)
            builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        // Generate QR button (stub)
        val generateQRButton: Button = view.findViewById(R.id.buttonGenerateQR)
        generateQRButton.setOnClickListener {
            Snackbar.make(view, "Generate QR clicked", Snackbar.LENGTH_SHORT).show()
        }

        // Other payment button (stub)
        val otherPaymentButton: Button = view.findViewById(R.id.buttonOtherPayment)
        otherPaymentButton.setOnClickListener {
            Snackbar.make(view, "Other Payment Methods clicked", Snackbar.LENGTH_SHORT).show()
        }
    }
}
