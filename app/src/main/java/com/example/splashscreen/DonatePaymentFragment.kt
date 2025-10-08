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

        // back button
        view.findViewById<ImageView>(R.id.buttonBack)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // zapper dialog
        view.findViewById<Button>(R.id.btnZapper)?.setOnClickListener {
            val b = AlertDialog.Builder(requireContext())
            b.setTitle("Zapper QR")
            val img = ImageView(requireContext()).apply {
                setImageResource(R.drawable.zapper)
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(32, 32, 32, 16)
            }
            b.setView(img)
            b.setPositiveButton("Close") { d, _ -> d.dismiss() }
            b.show()
        }

        // eft dialog
        view.findViewById<Button>(R.id.btnEFT)?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("EFT details")
                .setMessage(
                    "Ikusasalethu Baby Home\n" +
                            "Standard Bank\n" +
                            "Hillcrest\n" +
                            "Account number 052660093\n" +
                            "Branch code 045726\n" +
                            "Reference Your Name"
                )
                .setPositiveButton("Close") { d, _ -> d.dismiss() }
                .show()
        }

        // babychino dialog
        view.findViewById<Button>(R.id.btnBabychino)?.setOnClickListener {
            val b = AlertDialog.Builder(requireContext())
            b.setTitle("BabyChino QR")
            val img = ImageView(requireContext()).apply {
                setImageResource(R.drawable.babychino_qr)
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(32, 32, 32, 16)
            }
            b.setView(img)
            b.setPositiveButton("Close") { d, _ -> d.dismiss() }
            b.show()
        }

        // done button
        view.findViewById<Button>(R.id.button)?.setOnClickListener {
            Snackbar.make(view, "Thank you for your donation", Snackbar.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}
