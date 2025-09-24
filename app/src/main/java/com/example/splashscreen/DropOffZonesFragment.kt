package com.example.splashscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class DropOffZonesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_drop_off_zones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get directions button > Google Maps
        val directionsButton: Button = view.findViewById(R.id.buttonGetDirections)
        directionsButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=03 West Riding Road, Hillcrest, KZN")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        // Call us button
        val callButton: Button = view.findViewById(R.id.buttonCallUs)
        callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:0317651234") // replace with correct number
            startActivity(intent)
        }

        // Open in maps
        val openMaps: View = view.findViewById(R.id.buttonOpenMaps)
        openMaps.setOnClickListener {
            Snackbar.make(view, "Opening in Google Maps…", Snackbar.LENGTH_SHORT).show()
            val gmmIntentUri = Uri.parse("geo:0,0?q=03 West Riding Road, Hillcrest, KZN")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }
}
