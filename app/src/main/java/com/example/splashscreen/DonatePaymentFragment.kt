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

        // Back button returns to DonateFragment
        val backBtn = view.findViewById<ImageView>(R.id.buttonBack)
        backBtn?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Zapper button — opens BabyChino QR dialog (example)
        val zapperBtn = view.findViewById<Button>(R.id.btnZapper)
        zapperBtn?.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Zapper QR")

            val qrView = ImageView(requireContext())
            qrView.setImageResource(R.drawable.zapper)
            qrView.adjustViewBounds = true
            qrView.setPadding(32, 32, 32, 32)
            qrView.scaleType = ImageView.ScaleType.FIT_CENTER

            builder.setView(qrView)
            builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        // EFT Button
        val eftBtn = view.findViewById<Button>(R.id.btnEFT)
        eftBtn?.setOnClickListener {
            Snackbar.make(view, "Please use our bank details for EFT donation.", Snackbar.LENGTH_LONG).show()
        }

        // BabyChino Button
        val babychinoBtn = view.findViewById<Button>(R.id.btnBabychino)
        babychinoBtn?.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("BabyChino QR")

            val qrView = ImageView(requireContext())
            qrView.setImageResource(R.drawable.babychino_qr)
            qrView.adjustViewBounds = true
            qrView.setPadding(32, 32, 32, 32)
            qrView.scaleType = ImageView.ScaleType.FIT_CENTER

            builder.setView(qrView)
            builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        // Generate QR Button
        val generateQRButton = view.findViewById<Button>(R.id.buttonGenerateQR)
        generateQRButton?.setOnClickListener {
            Snackbar.make(view, "Generate Payment QR clicked", Snackbar.LENGTH_SHORT).show()
        }

        // Other Payment Methods Button
        val otherPaymentButton = view.findViewById<Button>(R.id.buttonOtherPayment)
        otherPaymentButton?.setOnClickListener {
            Snackbar.make(view, "Other Payment Methods clicked", Snackbar.LENGTH_SHORT).show()
        }

        // Done button
        val doneButton = view.findViewById<Button>(R.id.button)
        doneButton?.setOnClickListener {
            Snackbar.make(view, "Thank you for your donation!", Snackbar.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
